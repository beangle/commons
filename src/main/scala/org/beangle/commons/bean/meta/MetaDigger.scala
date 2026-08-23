/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.bean.meta

import org.beangle.commons.bean.meta.MetaModel.ParamHolder
import org.beangle.commons.bean.meta.MetaModel.ClassMeta
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.noreflect
import org.beangle.commons.lang.reflect.BeanInfo.Builder.getPropertyName

import scala.collection.mutable
import scala.quoted.*

/** Compile-time ClassMeta digger (quoted macro).
  *
  * MetaDigger: compile-time ClassMeta digger: same field discovery,
  * accessor detection and constructor/default resolution, but it drives a
  * [[MetaModel.Builder]] to produce [[ClassMeta]] directly — the future main
  * construction path of BeanInfo will rely on ClassMeta.
  */
object MetaDigger {
  /** Macro: digs ClassMeta for each class. */
  def digInto(argsExpr: Expr[Seq[Class[_]]])(using Quotes): Expr[List[ClassMeta]] = {
    import quotes.reflect.*
    argsExpr match {
      case Varargs(cls) =>
        val cmList = cls.map { cl =>
          cl.asTerm match {
            case TypeApply(term, trees) => new MetaDigger[quotes.type](trees.head.tpe).dig()
          }
        }
        Expr.ofList(cmList)
      case _ =>
        report.error(s"Args must be explicit", argsExpr)
        '{ ??? }
    }
  }

  /** Macro: digs ClassMeta for type T. */
  def digInto[T: Type](ec: Expr[Class[T]])(using Quotes): Expr[ClassMeta] = {
    val digger = new MetaDigger[quotes.type](quotes.reflect.TypeRepr.of[T])
    digger.dig()
  }
}

/** Macro-time type digger for ClassMeta. */
class MetaDigger[Q <: Quotes](trr: Any)(using val q: Q) {

  import q.reflect.*

  /** The TypeRepr being digested. */
  val typeRepr = trr.asInstanceOf[TypeRepr]

  /** Methods ignored like BeanInfo.ignores (runtime isFineMethod). */
  private val ignores = Set("hashCode", "toString", "wait", "clone", "equals", "getClass", "notify", "notifyAll", "apply", "unapply", "canEqual")
  private val caseIgnores = Set("productArity", "productIterator", "productPrefix", "productElement", "productElementName", "productElementNames", "copy")

  /** Produces Expr[ClassMeta] for the type. */
  def dig(): Expr[ClassMeta] = {
    '{
      val b = new MetaModel.Builder(${ typeOf(typeRepr) })
      ${ Expr.block(addMemberBody('b), 'b) }.build()
    }
  }

  /** Converts TypeRepr to Expr[Class[?]]. */
  def typeOf(tpe: TypeRepr): Expr[Class[?]] =
    Literal(ClassOfConstant(tpe)).asExpr.asInstanceOf[Expr[Class[?]]]

  /** Field expression for macro (name, type, get/set flags, accessor method names). */
  case class FieldExpr(
    name: String,
    typeinfo: Expr[AnyRef],
    transntAnnotated: Boolean,
    hasGet: Boolean,
    hasSet: Boolean,
    getterName: Option[String] = None,
    setterName: Option[String] = None
  ) {
    /** Returns true if this field should be transient. */
    def transnt(constructParamNames: Set[String]): Boolean = {
      if transntAnnotated then true else !constructParamNames.contains(name) && !hasSet
    }
  }

  /** Parameter expression for macro. */
  case class ParamExpr(name: String, typeinfo: Expr[AnyRef], defaultValue: Option[Expr[Any]] = None)

  /** Returns true if the name is a normal identifier (no $ or leading _). */
  def isNormal(name: String): Boolean = {
    !name.contains("$") && !name.startsWith("_")
  }

  /** Encodes a Scala decoded method name to its JVM bytecode form.
    * Uses [[scala.reflect.NameTransformer.encode]] to handle all operator characters
    * (e.g., ":" → "$colon", ">" → "$greater", "=" → "$eq", etc.).
    */
  private def encodeBytecodeName(name: String): String = {
    scala.reflect.NameTransformer.encode(name)
  }

  /** Extracts (isGetter, propertyName) from DefDef if it is an accessor. */
  def findAccessor(m: DefDef): Option[(Boolean, String)] = {
    val name = m.name
    var paramSize = 0
    m.paramss foreach {
      case TermParamClause(ps: List[ValDef]) => paramSize += ps.size
      case _ =>
    }
    if isNormal(name) then
      // Getter: no parameter lists at all (paramss is empty), non-Unit return
      // Method with empty params like `def foo(): Int` has paramss.size == 1 but paramSize == 0
      if (m.paramss.isEmpty && m.returnTpt.tpe.typeSymbol != Symbol.classSymbol(classOf[Unit].getName)) {
        Some((true, getPropertyName(name, true)))
      } else if (1 == paramSize) {
        val propertyName = getPropertyName(name, false)
        if (null != propertyName) Some((false, propertyName)) else None
      } else None
    else None
  }

  private def addMemberBody(t: Expr[MetaModel.Builder]): List[Expr[_]] = {
    val fieldMap = new mutable.HashMap[String, FieldExpr]
    val typeSymbol = typeRepr.typeSymbol
    val isScalaClass = !typeSymbol.flags.is(Flags.JavaDefined)
    val isCaseClass = typeRepr.typeSymbol.caseFields.nonEmpty
    val ctorDeclarations = typeSymbol.declarations.filter(_.isClassConstructor).toBuffer
    // dotty will add this(x01:Unit) method in class as primary constructor,we ignore it.
    ctorDeclarations -= typeSymbol.primaryConstructor
    if isScalaClass then ctorDeclarations.prepend(typeSymbol.primaryConstructor)

    val ctorDefaults = resolveCtorDefaults(typeSymbol)
    var i = 0
    val ctors = ctorDeclarations.map { s =>
      val defdef = s.tree.asInstanceOf[DefDef]
      i += 1
      resolveDefParams(defdef, Map.empty, if i == 1 then ctorDefaults else Map.empty)
    }

    val superBases = Set("scala.Any", "scala.Matchable", "java.lang.Object", "scala.Equals", "scala.Product", "java.io.Serializable")
    val methodDecls = new mutable.LinkedHashMap[String, (String, List[Expr[AnyRef]])] // dedup by name+signature, most-derived first
    for (bc <- typeRepr.baseClasses if !superBases.contains(bc.fullName)) {
      val base = typeRepr.baseType(bc)
      var params = Map.empty[String, TypeRepr]
      base match {
        case a: AppliedType => params = resolveClassTypes(a)
        case _ =>
      }

      //Some fields declared in primary constructor will by ignored due to missing public access methods.
      //So we discover declared fields,they may appear in that collection.
      base.typeSymbol.declaredFields foreach { mm =>
        val tpe = mm.tree.asInstanceOf[ValDef].tpt.tpe
        val transnt = mm.annotations exists (x => x.show.toLowerCase.contains("transient"))
        val noreflect = mm.hasAnnotation(Symbol.classSymbol(classOf[noreflect].getName))
        val isPublic = !mm.flags.is(Flags.Protected) && !mm.flags.is(Flags.Private)
        val isInnerType = mm.name == Strings.substringBetween(mm.tree.show, "this.", ".type")
        if isPublic && isNormal(mm.name) && !noreflect && !isInnerType then fieldMap.put(mm.name, FieldExpr(mm.name, resolveType(tpe, params), transnt, true, true))
      }

      base.typeSymbol.declaredMethods foreach { mm =>
        val defdef = mm.tree.asInstanceOf[DefDef]
        val isPublic = !defdef.symbol.flags.is(Flags.Protected) && !defdef.symbol.flags.is(Flags.Private)
        val ignored = isCaseClass && caseIgnores.contains(defdef.name) || ignores.contains(defdef.name)
        val noreflect = defdef.symbol.hasAnnotation(Symbol.classSymbol(classOf[noreflect].getName))
        val isStatic = defdef.symbol.flags.is(Flags.JavaStatic)
        if (isPublic && isNormal(defdef.name) && !ignored && !noreflect && !isStatic) {
          var paramSize = 0
          defdef.paramss.foreach {
            case TermParamClause(ps) => paramSize += ps.size
            case _ =>
          }
          val methodName = encodeBytecodeName(defdef.name)
          this.findAccessor(defdef) match {
            case Some((readable, name)) =>
              fieldMap.get(name) match {
                case Some(fx) =>
                  if readable then fieldMap.put(name, fx.copy(hasGet = true, getterName = Some(methodName)))
                  else fieldMap.put(name, fx.copy(hasSet = true, setterName = Some(methodName)))
                case None =>
                  val rtType = resolveType(defdef.returnTpt.tpe, params)
                  val paramList = resolveDefParams(defdef, params, Map.empty)
                  val transnt = defdef.symbol.annotations exists (x => x.show.toLowerCase.contains("transient"))
                  val fe = if readable then FieldExpr(name, rtType, transnt, true, false, getterName = Some(methodName))
                           else FieldExpr(name, paramList.head.typeinfo, transnt, false, true, setterName = Some(methodName))
                  fieldMap.put(name, fe)
              }
            case None =>
              // non-accessor method: collect name + TypeInfo for each param
              val paramTypeInfos = defdef.paramss.flatMap {
                case TermParamClause(ps: List[ValDef]) =>
                  ps.map(vl => resolveType(vl.tpt.tpe, params))
                case _ => Nil
              }
              val bytecodeName = encodeBytecodeName(defdef.name)
              val dedupKey = bytecodeName + paramTypeInfos.map(_.show).mkString("(", ",", ")")
              methodDecls.put(dedupKey, (bytecodeName, paramTypeInfos))
          }
        }
      }
    }
    val members = new mutable.ArrayBuffer[Expr[_]]()
    if !(ctors.size == 1 && ctors.head.isEmpty) then
      members ++= ctors.map { m =>
        val paramInfos = m.map { p =>
          if (p.defaultValue.isEmpty) '{ new ParamHolder(${ Expr(p.name) }, ${ p.typeinfo }) }
          else '{ new ParamHolder(${ Expr(p.name) }, ${ p.typeinfo }, Some(${ p.defaultValue.get })) }
        }
        '{ ${ t }.addCtor(Array(${ Varargs(paramInfos) }: _*)) }
      }
    end if

    val primaryCtorParamNames = ctors.headOption match {
      case Some(ctor) => ctor.map(_.name).toSet
      case None => Set.empty
    }

    val transients = fieldMap.values.filter(x => x.transnt(primaryCtorParamNames)).map(_.name).toSet
    members ++= fieldMap.values.map { x =>
      val getterExpr = x.getterName.map(n => '{ Some(${ Expr(n) }) }).getOrElse('{ None })
      val setterExpr = x.setterName.map(n => '{ Some(${ Expr(n) }) }).getOrElse('{ None })
      '{ ${ t }.addProperty(${ Expr(x.name) }, ${ x.typeinfo }, ${ Expr(transients.contains(x.name)) }, $getterExpr, $setterExpr) }
    }
    members ++= methodDecls.values.map { case (name, paramTypeInfos) =>
      '{ ${ t }.addMethod(${ Expr(name) }, Array(${ Varargs(paramTypeInfos) }: _*)) }
    }
    members.toList
  }

  /** Resolves TypeRepr to Expr[AnyRef] (Class or Array[Class, TypeInfo[]]). */
  def resolveType(typeRepr: TypeRepr, params: Map[String, TypeRepr]): Expr[AnyRef] = {
    var tpe = typeRepr
    var args: List[Expr[AnyRef]] = List.empty
    tpe match {
      case d: TypeRef => if (tpe.typeSymbol.flags.is(Flags.Param) && params.contains(tpe.typeSymbol.name)) tpe = params(tpe.typeSymbol.name)
      case c: AppliedType => args = resolveParamTypes(c, params)
      case d: AnnotatedType => tpe = d.underlying
      case c: ConstantType =>
      case n: OrType =>
      case _ => throw new RuntimeException("Unsupported type: " + tpe)
    }
    if args.isEmpty then typeOf(tpe)
    else '{ Array(${ typeOf(tpe) }, Array(${ Varargs(args) }: _*)) }
  }

  /** Resolves AppliedType's type args to a map of param name -> TypeRepr. */
  def resolveClassTypes(a: AppliedType, ctx: Map[String, TypeRepr] = Map.empty): Map[String, TypeRepr] = {
    val params = new mutable.HashMap[String, TypeRepr]
    val mts = a.typeSymbol.typeMembers
    var i = 0
    a.args foreach { arg =>
      val argType = if (arg.typeSymbol.flags.is(Flags.Param)) then ctx(arg.typeSymbol.name) else arg
      params.put(mts(i).name, argType)
      i += 1
    }
    params.toMap
  }

  /** Resolves AppliedType args to List[Expr[AnyRef]]. */
  def resolveParamTypes(a: AppliedType, ctx: Map[String, TypeRepr] = Map.empty): List[Expr[AnyRef]] = {
    val params = new mutable.ArrayBuffer[Expr[AnyRef]]
    a.args foreach { arg =>
      arg match {
        case d: TypeRef =>
          val argType = if arg.typeSymbol.flags.is(Flags.Param) && ctx.contains(arg.typeSymbol.name) then ctx(arg.typeSymbol.name) else d
          params += typeOf(argType)
        case c: AppliedType =>
          params += '{ Array(${ typeOf(c) }, Array(${ Varargs(resolveParamTypes(c, ctx)) }: _*)) }
        case tb: TypeBounds => typeOf(tb)
      }
    }
    params.toList
  }

  /** Resolves default parameter values from companion object. */
  def resolveCtorDefaults(symbol: Symbol): Map[Int, Expr[Any]] = {
    val comp = symbol.companionClass
    if (comp != Symbol.noSymbol) {
      val body = comp.tree.asInstanceOf[ClassDef].body
      val idents: List[(Int, Expr[Any])] =
        for case deff@DefDef(name, _, _, _) <- body
            if name.startsWith("$lessinit$greater$default$")
        yield (name.substring("$lessinit$greater$default$".length).toInt, Ref(deff.symbol).asExpr)
      idents.toMap
    } else {
      Map.empty
    }
  }

  /** Resolves DefDef parameters to ParamExpr list. */
  def resolveDefParams(defdef: DefDef, typeParams: Map[String, TypeRepr], defaults: Map[Int, Expr[Any]]): List[ParamExpr] = {
    val paramList = new mutable.ArrayBuffer[ParamExpr]
    defdef.paramss foreach {
      case TermParamClause(ps: List[ValDef]) =>
        paramList ++= ps.zipWithIndex.map { case (vl, idx) =>
          ParamExpr(vl.name, resolveType(vl.tpt.tpe, typeParams), defaults.get(idx + 1))
        }
      case _ =>
    }
    paramList.toList
  }
}
