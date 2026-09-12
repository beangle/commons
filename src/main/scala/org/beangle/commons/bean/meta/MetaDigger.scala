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
import org.beangle.commons.bean.meta.MetaModel.BeanMeta
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.{noreflect, property}
import org.beangle.commons.bean.meta.MetaLoader.getPropertyName

import scala.collection.mutable
import scala.quoted.*

/** Compile-time BeanMeta digger (quoted macro).
  *
  * MetaDigger: compile-time BeanMeta digger: same field discovery,
  * accessor detection and constructor/default resolution, but it drives a
  * [[MetaModel.Builder]] to produce [[BeanMeta]] directly — the future main
  * construction path of BeanInfo will rely on BeanMeta.
  */
object MetaDigger {
  /** Macro: digs BeanMeta for each class. */
  def digInto(argsExpr: Expr[Seq[Class[_]]])(using Quotes): Expr[List[BeanMeta]] = {
    import quotes.reflect.*

    /** Extracts the static type from a class literal, unwrapping inline/constant forms. */
    def classTypeOf(term: Term): TypeRepr = term match {
      case TypeApply(_, trees) => trees.head.tpe
      case Literal(ClassOfConstant(tpe)) => tpe
      case Inlined(_, _, expansion) => classTypeOf(expansion)
      case other => report.errorAndAbort(s"Unsupported class argument: ${other.show}")
    }

    argsExpr match {
      case Varargs(cls) =>
        val cmList = cls.map { cl => new MetaDigger[quotes.type](classTypeOf(cl.asTerm)).dig() }
        Expr.ofList(cmList)
      case _ =>
        report.error(s"Args must be explicit", argsExpr)
        '{ ??? }
    }
  }

  /** Macro: digs BeanMeta for type T. */
  def digInto[T: Type](ec: Expr[Class[T]])(using Quotes): Expr[BeanMeta] = {
    val digger = new MetaDigger[quotes.type](quotes.reflect.TypeRepr.of[T])
    digger.dig()
  }
}

/** Macro-time type digger for BeanMeta. */
class MetaDigger[Q <: Quotes](trr: Any)(using val q: Q) {

  import q.reflect.*

  /** The TypeRepr being digested. */
  val typeRepr = trr.asInstanceOf[TypeRepr]

  /** Produces Expr[BeanMeta] for the type.
   * Scala types are dug at compile time from symbol trees; Java-defined types follow
   * the standard JavaBean convention (getXxx/isXxx/setXxx) via runtime reflection
   * ([[MetaLoader]]), since their member trees are unavailable to the macro.
   * JDK/Scala library types (java./scala. prefix or $$) yield an empty BeanMeta.
   */
  def dig(): Expr[BeanMeta] = {
    val sym = typeRepr.dealias.typeSymbol
    if sym.flags.is(Flags.JavaDefined) then
      val className = sym.fullName
      if className.startsWith("java.") || className.startsWith("scala.") || className.contains("$$") then
        '{ new MetaModel.Builder(${ typeOf(typeRepr) }).build() }
      else
        '{ MetaLoader.load(${ typeOf(typeRepr) }) }
    else
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
    getterName: String,
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

  /** JavaBean 风格 getter 名（getXxx/isXxx），与 MetaLoader.isJavaBeanGetter 对齐。 */
  private def isJavaBeanGetterName(name: String): Boolean = {
    if name.startsWith("get") && name.length > 3 && name.charAt(3).isUpper then true
    else name.startsWith("is") && name.length > 2 && name.charAt(2).isUpper
  }

  /** JavaBean 风格 setter 名（setXxx）。 */
  private def isJavaBeanSetterName(name: String): Boolean =
    name.startsWith("set") && name.length > 3 && name.charAt(3).isUpper

  /** `@property` 注解声明的属性名：value 为空取方法名，否则取 value。
    *
    * 注解只作用于参数个数为 0（非 Unit 返回）的方法；带参方法上的注解一律忽略。
    * 与 [[MetaLoader.annotatedPropertyName]] 对齐：显式注解允许源码上的 `def foo(): T`
    * 也被识别（字节码层面同样是 0 参）。
    */
  private def annotatedPropertyName(m: DefDef, paramSize: Int): Option[String] = {
    if (0 != paramSize || isUnitType(m.returnTpt)) None
    else
      m.symbol.getAnnotation(Symbol.classSymbol(classOf[property].getName)) match {
        case Some(term) =>
          val value = annotationValue(term)
          Some(if (value.isEmpty) m.name else value)
        case None => None
      }
  }

  /** 取注解实参中的 value 字面量；无显式实参（走默认值）时返回空串。 */
  private def annotationValue(term: Term): String = term match {
    case Apply(_, args) =>
      args.collectFirst {
        case NamedArg("value", Literal(StringConstant(v))) => v
        case Literal(StringConstant(v)) => v
      }.getOrElse("")
    case Typed(inner, _) => annotationValue(inner)
    case Inlined(_, _, inner) => annotationValue(inner)
    case _ => ""
  }

  /** 是否为 Unit 返回类型。注意 `classOf[Unit].getName` 是 "void"，
    * 在 TASTy 中匹配不到 `scala.Unit`，需按类型比较。 */
  private def isUnitType(tpt: TypeTree): Boolean =
    tpt.tpe =:= TypeRepr.of[Unit]

  /** scala./java. 标准库基类。其中大部分是集合与框架方法（Seq 的 head/toList/size
    * 会把 Page 实现膨胀成几十个属性），但仍可能有 JavaBean 命名的成员（isEmpty/isTraversableAgain），
    * 这类成员与 [[MetaLoader]]/[[MetaLoaderLite]] 保持一致，不因定义在标准库而被丢弃。
    *
    * java.* 基类在编译期拿不到成员树，dig 不扫（[[MetaLoader.supports]] 也拒绝把 JDK 类
    * 作为运行期反射入口），其 JavaBean 属性由上层、且是工程内的 Java 父类经 javaBases 合并。 */
  private def isLibraryBase(fullName: String): Boolean =
    fullName.startsWith("scala.") || fullName.startsWith("java.")

  /** Extracts (isGetter, propertyName) from DefDef if it is an accessor. */
  def findAccessor(m: DefDef): Option[(Boolean, String)] = {
    val name = m.name
    var paramSize = 0
    m.paramss foreach {
      case TermParamClause(ps: List[ValDef]) => paramSize += ps.size
      case _ =>
    }
    if isNormal(name) then
      annotatedPropertyName(m, paramSize) match {
        case Some(propertyName) => Some((true, propertyName))
        case None =>
          // Getter: no parameter lists at all (paramss is empty), non-Unit return
          // Method with empty params like `def foo(): Int` has paramss.size == 1 but paramSize == 0
          if (m.paramss.isEmpty && !isUnitType(m.returnTpt)) {
            Some((true, getPropertyName(name, true)))
          } else if (1 == paramSize) {
            val propertyName = getPropertyName(name, false)
            if (null != propertyName) Some((false, propertyName)) else None
          } else None
      }
    else None
  }

  private def addMemberBody(t: Expr[MetaModel.Builder]): List[Expr[_]] = {
    val fieldMap = new mutable.HashMap[String, FieldExpr]
    val setterMap = new mutable.HashMap[String, String]
    val javaBases = new mutable.ArrayBuffer[TypeRepr]
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

    // scala.* 基类按 JavaBean 名称收录（见 isLibraryBase）；java.* 基类无成员树，直接跳过。
    for (bc <- typeRepr.baseClasses if !bc.fullName.startsWith("java.")) {
      val base = typeRepr.baseType(bc)
      val libraryBase = isLibraryBase(bc.fullName)
      var params = Map.empty[String, TypeRepr]
      base match {
        case a: AppliedType => params = resolveClassTypes(a)
        case _ =>
      }

      // Java 父类/接口的成员树不可用，运行期经 MetaLoader 反射合并其可读属性，
      // 使编译期 dig 出的 BeanMeta（写入 beanmeta.idx）覆盖继承的 JavaBean 属性。
      if bc.flags.is(Flags.JavaDefined) then
        val baseName = bc.fullName
        if !baseName.startsWith("java.") && !baseName.startsWith("scala.") && !baseName.contains("$$") then
          javaBases += base

      //Some fields declared in primary constructor will by ignored due to missing public access methods.
      //So we discover declared fields,they may appear in that collection.
      if !libraryBase then base.typeSymbol.declaredFields foreach { mm =>
        if !mm.flags.is(Flags.JavaDefined) then
          val tpe = mm.tree.asInstanceOf[ValDef].tpt.tpe
          val transnt = mm.annotations exists (x => x.show.toLowerCase.contains("transient"))
          val noreflect = mm.hasAnnotation(Symbol.classSymbol(classOf[noreflect].getName))
          val isPublic = !mm.flags.is(Flags.Protected) && !mm.flags.is(Flags.Private)
          val isInnerType = mm.name == Strings.substringBetween(mm.tree.show, "this.", ".type")
          // TASTy 把嵌套类/对象表示为合成 lazy val 模块字段（Flags.Module），
          // 不是真实 bean 属性（其类型为单例 TermRef，运行期 classOf 会引用不存在的
          // `$` 伴生类），直接跳过。
          val isModuleField = mm.flags.is(Flags.Module)
          // In Scala 3, var/val getters are implicit (not in declaredMethods).
          // Set getterName = field name since the getter method name matches the field name.
          if isPublic && isNormal(mm.name) && !noreflect && !isInnerType && !isModuleField then fieldMap.put(mm.name, FieldExpr(mm.name, resolveType(tpe, params), transnt, true, true, getterName = mm.name))
      }

      base.typeSymbol.declaredMethods foreach { mm =>
        if !mm.flags.is(Flags.JavaDefined) then
          val defdef = mm.tree.asInstanceOf[DefDef]
          val isPublic = !defdef.symbol.flags.is(Flags.Protected) && !defdef.symbol.flags.is(Flags.Private)
          val ignored = isCaseClass && MetaLoader.caseIgnores.contains(defdef.name) || MetaLoader.ignores.contains(defdef.name)
          val noreflect = defdef.symbol.hasAnnotation(Symbol.classSymbol(classOf[noreflect].getName))
          val isStatic = defdef.symbol.flags.is(Flags.JavaStatic)
          if (isPublic && isNormal(defdef.name) && !ignored && !noreflect && !isStatic
            && (!libraryBase || isJavaBeanGetterName(defdef.name) || isJavaBeanSetterName(defdef.name))) {
            var paramSize = 0
            defdef.paramss.foreach {
              case TermParamClause(ps) => paramSize += ps.size
              case _ =>
            }
            val methodName = encodeBytecodeName(defdef.name)
            this.findAccessor(defdef) match {
              case Some((readable, name)) =>
                if readable then
                  fieldMap.get(name) match {
                    case Some(fx) =>
                      // 与 MetaLoader 的 puttable 守卫对齐：字段访问器（getterName == 属性名）优先，
                      // 仅 JavaBean 风格 getter 可被覆盖，保证 beanmeta 与运行期反射的 getterName 一致。
                      if isJavaBeanGetterName(fx.getterName) then
                        fieldMap.put(name, fx.copy(hasGet = true, getterName = methodName))
                      else fieldMap.put(name, fx.copy(hasGet = true))
                    case None =>
                      val rtType = resolveType(defdef.returnTpt.tpe, params)
                      val transnt = defdef.symbol.annotations exists (x => x.show.toLowerCase.contains("transient"))
                      fieldMap.put(name, FieldExpr(name, rtType, transnt, true, false, getterName = methodName))
                  }
                else
                  setterMap.put(name, methodName)
              case None => // skip non-accessor methods
            }
          }
      }
    }

    // 虚拟属性（def x; def x_=）的 setter 可能在 getter 之前声明，统一收集后回填，保证顺序无关。
    setterMap.foreach { (name, methodName) =>
      fieldMap.get(name).foreach { fx =>
        fieldMap.put(name, fx.copy(hasSet = true, setterName = Some(methodName)))
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
      val setterExpr = x.setterName.map(n => '{ Some(${ Expr(n) }) }).getOrElse('{ None })
      '{ ${ t }.addProperty(${ Expr(x.name) }, ${ x.typeinfo }, ${ Expr(transients.contains(x.name)) }, ${ Expr(x.getterName) }, $setterExpr) }
    }
    members ++= javaBases.map { base =>
      '{ ${ t }.addProperties(MetaModels.reflect(${ typeOf(base) }).properties) }
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
      case t: TermRef => tpe = t.widen
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
      try {
        val body = comp.tree.asInstanceOf[ClassDef].body
        val idents: List[(Int, Expr[Any])] =
          for case deff@DefDef(name, _, _, _) <- body
              if name.startsWith("$lessinit$greater$default$")
          yield (name.substring("$lessinit$greater$default$".length).toInt, Ref(deff.symbol).asExpr)
        idents.toMap
      } catch {
        case _: Throwable => Map.empty
      }
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
