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

import org.beangle.commons.bean.meta.MetaModel.{BeanMeta, Ctor, Param, Property}
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.{noreflect, property}
import org.beangle.commons.lang.reflect.{Reflections, TypeInfo}

import java.lang.Character.isUpperCase
import java.lang.reflect.{Field, Method => JMethod, Modifier, ParameterizedType, TypeVariable}
import scala.collection.immutable.ArraySeq
import scala.collection.mutable
import scala.reflect.*

/** Runtime reflection counterpart of [[MetaDigger]]: reflects a Class into [[BeanMeta]].
  *
  * This is the fallback path when no pre-built beanmeta.idx is available.
  * The returned BeanMeta is pure metadata (no MethodHandles); use
  * [[org.beangle.commons.lang.reflect.BeanInfo.from]] to reconstruct
  * a BeanInfo with accessor MethodHandles.
  *
  * {{{
  * val cm = MetaLoader.load(classOf[User])
  * val bi = BeanInfo.from(cm)  // adds MethodHandles
  * }}}
  */
object MetaLoader {

  private[meta] case class Accessor(method: JMethod, returnType: TypeInfo)

  /** True when a class can be reflected into BeanMeta: application classes only.
   *  JDK (`java.*`), Scala runtime (`scala.*`) and JVM-generated classes (name
   *  containing `$$`, e.g. anonymous/lambda classes) are excluded. */
  def supports(clazz: Class[?]): Boolean = {
    val className = clazz.getName
    !(className.startsWith("java.") || className.startsWith("scala.") || className.contains("$$"))
  }

  /** Reflects a class into BeanMeta.
    *
    * Single-pass over class hierarchy: collects fields, getters/setters
    * in one walk, avoiding repeated scans.
    */
  def load(clazz: Class[?]): BeanMeta = {
    if (!supports(clazz)) throw new RuntimeException("Cannot reflect class: " + clazz.getName)

    val isCase = TypeInfo.isCaseClass(clazz)
    val getters = new mutable.HashMap[String, Accessor]
    val setters = new mutable.HashMap[String, Accessor]
    val fields = new mutable.HashMap[String, Field]
    val accessed = new mutable.HashSet[Class[?]]
    // 先收集 setter 属性名，主遍历时据此放行「无同名字段但有配对 setter」的参数less getter，
    // 避免依赖 getDeclaredMethods 的返回顺序。
    val setterNames = collectSetterNames(clazz)
    var nextClass = clazz
    var paramTypes: collection.Map[String, Class[?]] = Map.empty

    // Single pass: walk class hierarchy to discover fields and accessors
    while (null != nextClass && classOf[AnyRef] != nextClass) {
      nextClass.getDeclaredFields foreach { f => fields += (f.getName -> f) }
      nextClass.getDeclaredMethods foreach { m =>
        processMethod(isCase, m, getters, setters, fields, paramTypes, setterNames)
      }
      navInterfaces(nextClass, accessed, getters, setters, fields, paramTypes, setterNames)
      val nextType = nextClass.getGenericSuperclass
      nextClass = nextClass.getSuperclass
      paramTypes = Reflections.deduceParamTypes(nextClass, nextType, paramTypes)
    }

    // Discover constructors with default values
    val defaultCtorParamValues = findDefaultCtorParams(clazz)
    val ctors = discoverConstructors(clazz, defaultCtorParamValues, paramTypes)
    val primaryCtorParamNames = if ctors.isEmpty then Set.empty else ctors.head.parameters.map(_.name).toSet

    // Build property declarations from single-pass results
    val properties = buildProperties(getters, setters, fields, primaryCtorParamNames, isCase)

    BeanMeta(clazz, properties, ctors)
  }

  /** Builds property declarations from discovered getters (with optional setters). */
  private[meta] def buildProperties(
    getters: mutable.HashMap[String, Accessor],
    setters: mutable.HashMap[String, Accessor],
    fields: mutable.HashMap[String, Field],
    primaryCtorParamNames: Set[String],
    isCase: Boolean
  ): Seq[Property] = {
    getters.map { (name, getter) =>
      val setter = setters.get(name)
      val isTransientAnnotated = fields.get(name).exists(f => Modifier.isTransient(f.getModifiers))
      val isTransient = checkTransient(isTransientAnnotated, setter.isDefined, primaryCtorParamNames.contains(name))
      val setterName = setter.map(_.method.getName)
      // Check for Option type
      getter.returnType match
        case o: TypeInfo.OptionType => Property(name, o.elementType, isTransient, isOptional = true, getter.method.getName, setterName)
        case other => Property(name, other, isTransient, isOptional = false, getter.method.getName, setterName)
    }.toSeq.sortBy(_.name)
  }

  /** Discovers constructors with default values. */
  private def discoverConstructors(
    clazz: Class[?],
    defaultCtorParamValues: Map[Int, Any],
    paramTypes: collection.Map[String, Class[?]]
  ): Seq[Ctor] = {
    var foundDefaultCtor = false
    clazz.getConstructors.map { ctor =>
      val params = new mutable.ArrayBuffer[Param](ctor.getParameterCount)
      ctor.getParameters foreach { p =>
        params += Param(p.getName, typeof(p.getType, p.getParameterizedType, paramTypes), None)
      }
      if (!foundDefaultCtor && defaultCtorParamValues.nonEmpty) {
        if (isDefaultParamMatched(defaultCtorParamValues, params)) {
          foundDefaultCtor = true
          defaultCtorParamValues foreach { case (idx, v) =>
            params(idx - 1) = params(idx - 1).copy(defaultValue = Some(v))
          }
        }
      }
      Ctor(ArraySeq.from(params))
    }.toSeq
  }

  /** Finds default constructor parameter values from companion object. */
  private def findDefaultCtorParams(clazz: Class[?]): Map[Int, Any] = {
    org.beangle.commons.lang.ClassLoaders.get(clazz.getName + "$") match {
      case Some(companionClass) =>
        val singleton = companionClass.getDeclaredField("MODULE$").get(null)
        val params = Collections.newMap[Int, Any]
        companionClass.getDeclaredMethods foreach { m =>
          val index = Strings.substringAfter(m.getName, "$lessinit$greater$default$")
          if (Strings.isNotEmpty(index)) params.put(Integer.parseInt(index), m.invoke(singleton))
        }
        params.toMap
      case None => Map.empty
    }
  }

  private def isDefaultParamMatched(defaultCtorParamValues: Map[Int, Any], params: collection.Seq[Param]): Boolean = {
    defaultCtorParamValues.forall { case (idx, pv) =>
      if ((idx - 1) < params.length) {
        pv match
          case null => true
          case value => org.beangle.commons.lang.Primitives.wrap(params(idx - 1).typeinfo.clazz)
            .isAssignableFrom(org.beangle.commons.lang.Primitives.wrap(value.getClass))
      } else {
        false
      }
    }
  }

  private def navInterfaces(
    clazz: Class[?],
    accessed: mutable.HashSet[Class[?]],
    getters: mutable.HashMap[String, Accessor],
    setters: mutable.HashMap[String, Accessor],
    fields: collection.Map[String, Field],
    paramTypes: collection.Map[String, Class[?]],
    setterNames: collection.Set[String]
  ): Unit = {
    if (null == clazz || classOf[AnyRef] == clazz) return
    val isCase = TypeInfo.isCaseClass(clazz)
    val interfaceTypes = clazz.getGenericInterfaces
    (0 until interfaceTypes.length) foreach { i =>
      val interface = interfaceTypes(i) match {
        case pt: ParameterizedType => pt.getRawType.asInstanceOf[Class[?]]
        case c: Class[_] => c
      }
      if (!accessed.contains(interface)) {
        accessed.add(interface)
        val interfaceParamTypes = Reflections.deduceParamTypes(interface, interfaceTypes(i), paramTypes)
        interface.getDeclaredMethods foreach { m =>
          processMethod(isCase, m, getters, setters, fields, interfaceParamTypes, setterNames)
        }
        navInterfaces(interface, accessed, getters, setters, fields, paramTypes, setterNames)
      }
    }
  }

  private[meta] def processMethod(
    isCase: Boolean,
    method: JMethod,
    getters: mutable.HashMap[String, Accessor],
    setters: mutable.HashMap[String, Accessor],
    fields: collection.Map[String, Field],
    paramTypes: collection.Map[String, Class[?]],
    setterNames: collection.Set[String] = Set.empty
  ): Unit = {
    if (isFineMethod(isCase, method, false) || isExplicitProperty(method)) {
      findAccessor(method, fields, setterNames) match {
        case Some((readable, name)) =>
          if (readable) {
            // 属性名访问器（字段访问器，或 def x / def x_$eq 配对）优先，仅 JavaBean 风格 getter 可被覆盖，
            // 保证 beanmeta 与运行期反射的 getterName 一致。
            val puttable = getters.get(name).forall(x => isJavaBeanGetter(x.method))
            if puttable then
              getters.put(name, Accessor(method, typeof(method.getReturnType, method.getGenericReturnType, paramTypes)))
          } else {
            val types = method.getGenericParameterTypes
            val clazzes = method.getParameterTypes
            val paramTypeInfos = new Array[TypeInfo](types.length)
            (0 until types.length) foreach { j => paramTypeInfos(j) = typeof(clazzes(j), types(j), paramTypes) }
            setters.put(name, Accessor(method, paramTypeInfos(0)))
          }
        case None => // skip non-accessor methods
      }
    }
  }

  private[meta] def isJavaBeanGetter(method: JMethod): Boolean = {
    val name = method.getName
    if name.startsWith("get") && name.length > 3 && isUpperCase(name.charAt(3)) then true
    else if name.startsWith("is") && name.length > 2 && isUpperCase(name.charAt(2)) then true
    else false
  }

  /** Resolves TypeInfo from Class and generic Type. */
  def typeof(clazz: Class[?], typ: java.lang.reflect.Type, paramTypes: collection.Map[String, Class[?]]): TypeInfo = {
    if TypeInfo.isIterableType(clazz) then
      if clazz.isArray then
        TypeInfo.get(clazz, clazz.getComponentType)
      else
        typ match {
          case pt: ParameterizedType =>
            if (pt.getActualTypeArguments.length == 1) TypeInfo.get(clazz, typeAt(pt, 0))
            else TypeInfo.get(clazz, typeAt(pt, 0), typeAt(pt, 1))
          case tv: TypeVariable[_] => TypeInfo.get(paramTypes.getOrElse(tv.getName, classOf[AnyRef]))
          case _: Class[_] => TypeInfo.get(clazz, false)
          case _ => TypeInfo.get(clazz, classOf[Any], classOf[Any])
        }
    else if clazz == classOf[Option[?]] then
      val innerType = typ match {
        case pt: ParameterizedType => if (pt.getActualTypeArguments.length == 1) typeAt(pt, 0) else classOf[AnyRef]
        case c: Class[_] => classOf[AnyRef]
      }
      TypeInfo.get(innerType, optional = true)
    else
      TypeInfo.get(clazz)
  }

  private def typeAt(typ: java.lang.reflect.Type, idx: Int): Class[?] = {
    typ match {
      case c: Class[_] => c
      case pt: ParameterizedType =>
        pt.getActualTypeArguments()(idx) match {
          case c: Class[_] => c
          case _ => classOf[AnyRef]
        }
      case _ => classOf[AnyRef]
    }
  }

  // --- Method classification utilities (used by MetaLoader and MetaDigger) ---

  val ignores = Set("hashCode", "toString", "wait", "clone", "equals", "getClass", "notify", "notifyAll") ++
    Set("apply", "unapply", "unApply", "canEqual")
  val caseIgnores = Set("productArity", "productIterator", "productPrefix", "productElement", "productElementName", "productElementNames", "copy")

  /** Returns true if property should be treated as transient. */
  def checkTransient(transientAnnotated: Boolean, hasSetter: Boolean, usedInPrimaryCtor: Boolean): Boolean = {
    if transientAnnotated then true else !usedInPrimaryCtor && !hasSetter
  }

  /** Returns true if method is a candidate for property accessor discovery. */
  def isFineMethod(isCase: Boolean, method: JMethod, allowBridge: Boolean = false): Boolean = {
    val modifiers = method.getModifiers
    val name = method.getName
    val ignored = ignores.contains(name) || (isCase && caseIgnores.contains(name))
    val modifierNice = !Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)
    !method.isAnnotationPresent(classOf[noreflect]) && !ignored && modifierNice && isFineMethodName(name) && (!method.isBridge || allowBridge)
  }

  /** Returns true if method name follows getter/setter convention. */
  private def isFineMethodName(name: String): Boolean = {
    if name.startsWith("_") then false
    else if name.endsWith("_$eq") then !name.substring(0, name.length - 4).contains("$")
    else !name.contains("$")
  }

  /** `@property` 声明的属性名：value 为空时取方法名，否则取 value。
    *
    * 注解只用于零参（非 Unit 返回）方法，即 Scala 中无法按 JavaBean 约定识别的
    * 参数less def（`def hasPrevious: Boolean`，字节码上与 `def size()` 无差别）；
    * 带参方法上的注解（含 setter）一律忽略，仍按原有规则识别。
    */
  private[meta] def annotatedPropertyName(method: JMethod): Option[String] = {
    if (0 != method.getParameterCount || method.getReturnType == classOf[Unit]) None
    else {
      val annotation = method.getAnnotation(classOf[property])
      if (null == annotation) None
      else if (annotation.value.isEmpty) Some(method.getName) else Some(annotation.value)
    }
  }

  /** 显式属性：`@property` 标注的 public 实例零参方法，`@noreflect` 仍可排除。 */
  private[meta] def isExplicitProperty(method: JMethod): Boolean = {
    annotatedPropertyName(method).isDefined &&
      !Modifier.isStatic(method.getModifiers) && Modifier.isPublic(method.getModifiers) &&
      !method.isAnnotationPresent(classOf[noreflect])
  }

  /** Returns (true, propertyName) for getter, (false, propertyName) for setter, or None.
    * Identifies accessor methods. For getters, only accepts JavaBean-style (getXxx/isXxx)
    * or methods matching a known field name — Scala parameterless methods like `def parents`
    * cannot be distinguished from empty-parens methods like `def size()` at bytecode level,
    * so they are excluded unless backed by a field or annotated with `@property`.
    */
  def findAccessor(method: JMethod, fields: collection.Map[String, Field]): Option[(Boolean, String)] = {
    findAccessor(method, fields, Set.empty)
  }

  /** Extends [[findAccessor]] with pairing evidence: a parameterless method also counts as a
    * getter when a same-named setter (`setXxx`/`x_$eq`/`x_=`) exists, i.e. a Scala-style
    * read-write accessor pair without a backing field (`def p1: T` + `def p1_=(v: T)`).
    * `setterNames` is the property-name set of setters declared anywhere in the hierarchy
    * (see [[collectSetterNames]]), so the verdict does not depend on declaration order.
    * Only members declared by the application class itself qualify: `scala.*`/`java.*` base
    * classes keep the JavaBean-naming / same-named-field rules (see [[pairsWithAppSetter]]).
    */
  def findAccessor(
    method: JMethod,
    fields: collection.Map[String, Field],
    setterNames: collection.Set[String]
  ): Option[(Boolean, String)] = {
    val name = method.getName
    val parameterTypes = method.getParameterTypes
    annotatedPropertyName(method) match {
      case Some(propertyName) => Some((true, propertyName))
      case None =>
        if (0 == parameterTypes.length && method.getReturnType != classOf[Unit]) {
          if (isJavaBeanGetter(method) || fields.contains(name) || pairsWithAppSetter(method, name, setterNames)) then
            Some((true, getPropertyName(name, true)))
          else None
        } else if (1 == parameterTypes.length) {
          val propertyName = getPropertyName(name, false)
          if (null != propertyName && !propertyName.contains("$")) Some((false, propertyName)) else None
        } else None
    }
  }

  /** 应用类内声明的一参 setter 佐证：getter 与 setter 都出自应用类时，才用「配对 setter」
    * 放行参数less getter；`scala.*`/`java.*` 基类仍只按 JavaBean 命名或同名字段识别，
    * 以保持 strict 与 lite/dig 的既有约定（lite ⊇ strict）。 */
  private def pairsWithAppSetter(method: JMethod, name: String, setterNames: collection.Set[String]): Boolean =
    setterNames.contains(name) && !isLibraryDeclared(method)

  /** 成员是否由 `scala.*`/`java.*` 基类声明（应用类自身的 trait 不算）。 */
  private[meta] def isLibraryDeclared(method: JMethod): Boolean = isLibraryClass(method.getDeclaringClass)

  /** 继承链（含接口）上**应用类**声明的一参 setter 属性名集合：`setXxx` → `xxx`，
    * `x_$eq`/`x_=` → `x`。
    *
    * 用于 [[findAccessor]] 放行「无同名字段、但有配对 setter」的参数less getter
    * （`def p1: T` 配 `def p1_=(v: T)`）。字节码层面参数less `def x` 与空括号 `def x()`
    * 无法区分，因此仍需 setter 作为佐证；必须在主遍历前完成，保证与声明顺序无关。
    */
  private def collectSetterNames(clazz: Class[?]): collection.Set[String] = {
    val names = new mutable.HashSet[String]
    val visited = new mutable.HashSet[Class[?]]
    def visit(c: Class[?]): Unit = {
      if (null != c && classOf[AnyRef] != c && !visited.contains(c)) {
        visited.add(c)
        if (!isLibraryClass(c)) {
          val isCase = TypeInfo.isCaseClass(c)
          c.getDeclaredMethods foreach { m =>
            if (1 == m.getParameterCount && isFineMethod(isCase, m, false)) {
              val name = getPropertyName(m.getName, false)
              if (null != name && !name.contains("$")) names.add(name)
            }
          }
        }
        c.getInterfaces foreach visit
        visit(c.getSuperclass)
      }
    }
    visit(clazz)
    names
  }

  private[meta] def isLibraryClass(clazz: Class[?]): Boolean = {
    val name = clazz.getName
    name.startsWith("scala.") || name.startsWith("java.")
  }

  /** Extracts property name from getter/setter method name. */
  def getPropertyName(name: String, getter: Boolean): String = {
    if (getter) {
      if (name.startsWith("get") && name.length > 3 && isUpperCase(name.charAt(3))) lower(name.substring(3))
      else if (name.startsWith("is") && name.length > 2 && isUpperCase(name.charAt(2))) lower(name.substring(2))
      else name
    } else {
      if (name.startsWith("set") && name.length > 3 && isUpperCase(name.charAt(3))) lower(name.substring(3))
      else if (name.endsWith("_$eq")) Strings.substringBefore(name, "_$eq")
      else if (name.endsWith("_=")) Strings.substringBefore(name, "_=")
      else null
    }
  }

  private def lower(name: String): String = {
    if (name.length > 1 && isUpperCase(name.charAt(1))) name else Strings.uncapitalize(name)
  }
}
