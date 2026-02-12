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

package org.beangle.commons.lang.reflect

import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.noreflect
import org.beangle.commons.lang.reflect.BeanInfo.*
import org.beangle.commons.lang.reflect.BeanInfo.Builder.{ParamHolder, getPropertyName}

import java.lang.reflect.Method
import scala.collection.immutable.ArraySeq
import scala.collection.mutable
import scala.quoted.*

/** Compile-time BeanInfo digger (quoted macro). */
object BeanInfoDigger {
  /** Macro: digs BeanInfo for each class and updates cache. */
  def digInto(argsExpr: Expr[Seq[Class[_]]], cache: Expr[BeanInfoCache])(using Quotes): Expr[List[BeanInfo]] = {
    import quotes.reflect.*
    argsExpr match {
      case Varargs(cls) =>
        val biList = cls.map { cl =>
          cl.asTerm match {
            case TypeApply(term, trees) => new BeanInfoDigger[quotes.type](trees.head.tpe).dig()
          }
        }
        '{
          val bis = ${ Expr.ofList(biList) }
          bis.foreach { bi => ${ cache }.update(bi) }
          bis
        }
      case _ =>
        report.error(s"Args must be explicit", argsExpr)
        '{ ??? }
    }
  }

  /** Macro: digs BeanInfo for type T and updates cache. */
  def digInto[T: Type](ec: Expr[Class[T]], cache: Expr[BeanInfoCache])(implicit quotes: Quotes): Expr[BeanInfo] = {
    import quotes.reflect.*
    val digger = new BeanInfoDigger[quotes.type](quotes.reflect.TypeRepr.of[T])
    '{
      ${ cache }.update(${ digger.dig() })
    }
  }
}

/** Macro-time type digger for BeanInfo. */
class BeanInfoDigger[Q <: Quotes](trr: Any)(using val q: Q) {

  import q.reflect.*

  /** The TypeRepr being digested. */
  val typeRepr = trr.asInstanceOf[TypeRepr]

  /** Produces Expr[BeanInfo] for the type. */
  def dig(): Expr[BeanInfo] = {
    '{
      val b = new BeanInfo.Builder(${ typeOf(typeRepr) })
      ${ Expr.block(addMemberBody('b), 'b) }.build()
    }
  }

  /** Converts TypeRepr to Expr[Class[?]]. */
  def typeOf(tpe: TypeRepr): Expr[Class[?]] =
    Literal(ClassOfConstant(tpe)).asExpr.asInstanceOf[Expr[Class[?]]]

  /** Field expression for macro (name, type, get/set flags). */
  case class FieldExpr(name: String, typeinfo: Expr[AnyRef], transntAnnotated: Boolean, hasGet: Boolean, hasSet: Boolean, defaultValue: Option[Expr[Any]] = None) {
    /** Returns true if this field should be transient. */
    def transnt(constructParamNames: Set[String]): Boolean = BeanInfo.Builder.isTransient(transntAnnotated, hasSet, constructParamNames.contains(name))
  }

  /** Parameter expression for macro. */
  case class ParamExpr(name: String, typeinfo: Expr[AnyRef], defaultValue: Option[Expr[Any]] = None)

  /** Method expression for macro (name, return type, params, asField). */
  case class MethodExpr(name: String, rt: Expr[Any], params: Seq[ParamExpr], asField: Boolean)

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

  private def addMemberBody(t: Expr[BeanInfo.Builder]): List[Expr[_]] = {
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
        val rtType = resolveType(defdef.returnTpt.tpe, params)
        val isPublic = !defdef.symbol.flags.is(Flags.Protected) && !defdef.symbol.flags.is(Flags.Private)
        val ignored = isCaseClass && Set("productPrefix", "productArity", "productIterator", "productElementNames").contains(defdef.name)
        if (isPublic && isNormal(defdef.name) && !ignored) {
          this.findAccessor(defdef) foreach { (readable, name) =>
            fieldMap.get(name) match {
              case Some(fx) => if readable then fieldMap.put(name, fx.copy(hasGet = true)) else fieldMap.put(name, fx.copy(hasSet = true))
              case None =>
                val paramList = resolveDefParams(defdef, params, Map.empty)
                val transnt = defdef.symbol.annotations exists (x => x.show.toLowerCase.contains("transient"))
                val fe = if readable then FieldExpr(name, rtType, transnt, true, false) else FieldExpr(name, paramList.head.typeinfo, transnt, false, true)
                fieldMap.put(name, fe)
            }
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

    val transients = fieldMap.values.filter(x => x.transnt(primaryCtorParamNames)).map(x => Expr(x.name)).toList
    if (transients.nonEmpty) {
      members += '{ ${ t }.addTransients(Array(${ Varargs(transients) }: _*)) }
    }
    members ++= fieldMap.values.map { x =>
      '{ ${ t }.addField(${ Expr(x.name) }, ${ x.typeinfo }) }
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
      case _ => throw new RuntimeException("Unspported type :" + tpe)
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
