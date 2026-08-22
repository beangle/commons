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
  * A copy of BeanInfoDigger's static-compilation logic: same field discovery,
  * accessor detection and constructor/default resolution, but it drives a
  * [[MetaModel.Builder]] to produce [[ClassMeta]] directly — the future main
  * construction path of BeanInfo will rely on ClassMeta.
  */
object ClassMetaDigger {
  /** Macro: digs ClassMeta for each class. */
  def digInto(argsExpr: Expr[Seq[Class[_]]])(using Quotes): Expr[List[ClassMeta]] = {
    import quotes.reflect.*
    argsExpr match {
      case Varargs(cls) =>
        val cmList = cls.map { cl =>
          cl.asTerm match {
            case TypeApply(term, trees) => new ClassMetaDigger[quotes.type](trees.head.tpe).dig()
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
    val digger = new ClassMetaDigger[quotes.type](quotes.reflect.TypeRepr.of[T])
    digger.dig()
  }
}

/** Macro-time type digger for ClassMeta. */
class ClassMetaDigger[Q <: Quotes](trr: Any)(using val q: Q) {

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

  /** Field expression for macro (name, type, get/set flags). */
  case class FieldExpr(name: String, typeinfo: Expr[AnyRef], transntAnnotated: Boolean, hasGet: Boolean, hasSet: Boolean) {
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

  /** Extracts (isGetter, propertyName) from DefDef if it is an accessor. */
  def findAccessor(m: DefDef): Option[(Boolean, String)] = {
    val name = m.name
    var paramSize = 0
    m.paramss foreach {
      case TermParamClause(ps: List[ValDef]) => paramSize += ps.size
      case _ =>
    }
    if isNormal(name) then
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
    val methodDecls = new mutable.LinkedHashMap[String, (String, List[String])] // dedup by name+signature, most-derived first
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
          this.findAccessor(defdef) match {
            case Some((readable, name)) =>
              fieldMap.get(name) match {
                case Some(fx) => if readable then fieldMap.put(name, fx.copy(hasGet = true)) else fieldMap.put(name, fx.copy(hasSet = true))
                case None =>
                  val rtType = resolveType(defdef.returnTpt.tpe, params)
                  val paramList = resolveDefParams(defdef, params, Map.empty)
                  val transnt = defdef.symbol.annotations exists (x => x.show.toLowerCase.contains("transient"))
                  val fe = if readable then FieldExpr(name, rtType, transnt, true, false) else FieldExpr(name, paramList.head.typeinfo, transnt, false, true)
                  fieldMap.put(name, fe)
              }
            case None =>
              // non-accessor method: collect name + erased JVM param types
              val paramNames = resolveParamJvmNames(defdef, params)
              methodDecls.put(defdef.name + paramNames.mkString("(", ",", ")"), (defdef.name, paramNames))
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
      '{ ${ t }.addProperty(${ Expr(x.name) }, ${ x.typeinfo }, ${ Expr(transients.contains(x.name)) }) }
    }
    members ++= methodDecls.values.map { case (name, paramNames) =>
      '{ ${ t }.addMethod(${ Expr(name) }, Array(${ Varargs(paramNames.map(Expr(_))) }: _*)) }
    }
    members.toList
  }

  /** Resolves erased JVM param type names for a method (e.g. "java/lang/String", "int"). */
  def resolveParamJvmNames(defdef: DefDef, params: Map[String, TypeRepr]): List[String] = {
    val names = new mutable.ListBuffer[String]
    defdef.paramss foreach {
      case TermParamClause(ps: List[ValDef]) =>
        ps foreach { vl => names += jvmName(resolveTypeRef(vl.tpt.tpe, params)) }
      case _ =>
    }
    names.toList
  }

  /** Resolves a TypeRepr through type params (like resolveType, without expr building). */
  def resolveTypeRef(tpe: TypeRepr, params: Map[String, TypeRepr]): TypeRepr = {
    var t = tpe
    t match {
      case d: TypeRef => if (t.typeSymbol.flags.is(Flags.Param) && params.contains(t.typeSymbol.name)) t = params(t.typeSymbol.name)
      case c: AppliedType =>
      case d: AnnotatedType => t = d.underlying
      case _ =>
    }
    t
  }

  /** Converts a TypeRepr to the JVM class name (java.lang.reflect style, dots to slashes). */
  def jvmName(tpe: TypeRepr): String = {
    val t = tpe.widenTermRefByName.dealias
    if (t =:= TypeRepr.of[Boolean]) "boolean"
    else if (t =:= TypeRepr.of[Byte]) "byte"
    else if (t =:= TypeRepr.of[Short]) "short"
    else if (t =:= TypeRepr.of[Char]) "char"
    else if (t =:= TypeRepr.of[Int]) "int"
    else if (t =:= TypeRepr.of[Long]) "long"
    else if (t =:= TypeRepr.of[Float]) "float"
    else if (t =:= TypeRepr.of[Double]) "double"
    else t match {
      case a: AppliedType if a.typeSymbol.fullName == "scala.Array" => arrayJvmName(jvmName(a.args.head))
      case _ =>
        t.classSymbol match
          case Some(sym) => sym.fullName.replace('.', '/')
          case None      => "java/lang/Object" // type param / unsupported → erased to Object
    }
  }

  /** Builds the JVM descriptor-ish name of an array whose element name is given. */
  def arrayJvmName(element: String): String = element match {
    case "boolean" => "[Z"
    case "byte"    => "[B"
    case "short"   => "[S"
    case "char"    => "[C"
    case "int"     => "[I"
    case "long"    => "[J"
    case "float"   => "[F"
    case "double"  => "[D"
    case n if n.startsWith("[") => n
    case n => "[L" + n + ";"
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
    var i = 0
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
      i += 1
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
        var i = 0
        paramList ++= ps.map { vl =>
          i += 1
          ParamExpr(vl.name, resolveType(vl.tpt.tpe, typeParams), defaults.get(i))
        }
      case _ =>
    }
    paramList.toList
  }
}
