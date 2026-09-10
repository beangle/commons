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

package org.beangle.commons.aot

import org.beangle.commons.collection.Collections

import scala.collection.mutable

/** Mutable container for ahead-of-time hints used by GraalVM native-image.
  *
  * Subclasses or callers register types, resource patterns, proxy interfaces,
  * serializable classes, and runtime-initialized classes via the register
  * methods, then pass this container to [[AotHintGenerator]] for config file
  * generation.
  *
  * {{{
  * val hints = new AotHints
  * hints.registerType(classOf[User], classOf[Role])
  * hints.registerPattern("META-INF/custom.idx")
  * hints.registerProxy(classOf[UserService])
  * hints.registerSerializable(classOf[UserDto])
  * AotHintGenerator.write(outDir, hints)
  * }}}
  *
  * 反射类型按 [[AotPolicy]] 逐类登记：简单路径 `registerType(clazz)` 使用容器默认策略
  * （通常来自 [[AotHintRegistrar.aotPolicy]]，默认 public 方法 + public 构造器、
  * 无字段、不递归）；定制路径 `registerType(clazz, policy)` 对单个类显式指定策略。
  * 需要运行时反射字段/方法元数据的场景可使用 [[AotPolicy.bean]] 策略。
  */
class AotHints(val policy: AotPolicy = AotPolicy.default) {

  private val typePolicies = mutable.LinkedHashMap.empty[Class[_], AotPolicy]
  private val patterns = Collections.newSet[String]
  private val proxies = Collections.newSet[List[String]]
  private val constructors = Collections.newSet[String]
  private val jniTypes = mutable.LinkedHashSet.empty[String]
  private val jniMethods = mutable.LinkedHashMap.empty[String, mutable.LinkedHashSet[(String, List[String])]]
  private val jniFields = mutable.LinkedHashMap.empty[String, mutable.LinkedHashSet[String]]
  private val serializables = Collections.newSet[Class[_]]
  private val runtimeInitialized = Collections.newSet[Class[_]]

  /** Packages whose reflection metadata GraalVM already provides; any
   *  `java.*`/`javax.*`/`jdk.*`/`sun.*`/`com.sun.*`/`scala.*` class is skipped in
   *  [[addType]] (recursive expansion and plain `registerType`) to avoid implicit
   *  JDK registrations. Explicit by-name registration of such types is still
   *  possible via [[registerConstructor]] / [[registerJniType]] (e.g. JDK URL
   *  protocol handlers, JNI callbacks into `sun.font.*`). */
  private val jdkPrefixes = Seq("java.", "javax.", "jdk.", "sun.", "com.sun.", "scala.")

  /** 简单路径：按容器默认策略（通常来自 registrar 的 `aotPolicy`）注册反射类型。 */
  def registerType(classes: Class[_]*): Unit = {
    val it = classes.iterator
    while it.hasNext do addType(it.next(), policy)
  }

  /** 定制路径：对单个类显式指定策略，例如 declared 成员、字段或递归父类。 */
  def registerType(clazz: Class[_], custom: AotPolicy): Unit = addType(clazz, custom)

  /** 按简单类名注册数组类型（`"java.sql.Statement"` → `[Ljava.sql.Statement;`），
   *  自动标记 unsafeAllocated，供 `Array.newInstance`/`Array.get` 等按名反射
   *  创建/访问数组元素。已是指针数组描述符（`[L...;`）时原样透传；基础类型名
   *  （如 `"int"` → `[I`）同样支持。类缺失（如 optional 依赖未引入）时静默跳过。 */
  def registerArrayOf(className: String, loader: ClassLoader): Unit = {
    val descriptor =
      if className.startsWith("[") then className
      else if primitiveDescriptors.contains(className) then "[" + primitiveDescriptors(className)
      else "[L" + className + ";"
    try {
      val clazz = Class.forName(descriptor, false, loader)
      addType(clazz, AotPolicy.array)
    } catch {
      case _: Throwable => ()
    }
  }

  /** 注册 Scala 3 枚举：枚举类、伴生对象（`MODULE$` + `values()`）以及全部值类
   *  （含带主体的匿名子类），并同步登记序列化（枚举值可能作为实体/缓存的一部分被
   *  序列化）。仅接受 [[scala.reflect.Enum]] 的下游类，其余类型抛
   *  `IllegalArgumentException`。
   *
   *  与 `registerType` 的区别：`registerType` 按给定策略只注册类本身，不感知枚举
   *  特性；`registerEnum` 显式注册枚举类、伴生对象、全部值类并同步序列化登记
   *  （反射按值读 id/ordinal 需要值类元数据）。
   *
   *  值类枚举通过伴生对象的静态 `MODULE$` 单例 + `values()` 反射获取，因此适用于
   *  顶层枚举；嵌套枚举（伴生无静态 `MODULE$`）无法枚举值类时静默跳过值注册。 */
  def registerEnum(enumType: Class[_]): Unit = {
    if !classOf[scala.reflect.Enum].isAssignableFrom(enumType) then
      throw new IllegalArgumentException(
        s"registerEnum requires a Scala 3 enum (scala.reflect.Enum subclass), got $enumType")
    try {
      val loader = enumType.getClassLoader
      addType(enumType, AotPolicy.enumPolicy)
      registerSerializable(enumType)
      val companion = Class.forName(enumType.getName + "$", false, loader)
      addType(companion, AotPolicy.enumPolicy)
      val values = companion.getMethod("values").invoke(companion.getField("MODULE$").get(null))
      values.asInstanceOf[Array[AnyRef]] foreach { v =>
        addType(v.getClass, AotPolicy.enumPolicy)
        registerSerializable(v.getClass)
      }
    } catch {
      case _: Throwable => ()
    }
  }

  /** Adds a class with the given policy; when recursive, expands the non-JDK
   *  superclass and interface hierarchy with the same policy. */
  private def addType(clazz: Class[_], p: AotPolicy): Unit = {
    if (clazz == null || isJdk(clazz)) return
    merge(clazz, p)
    if p.recursive then
      addType(clazz.getSuperclass, p)
      clazz.getInterfaces foreach (addType(_, p))
  }

  /** Java 基础类型名 → JVM 数组描述符元素码（如 `"int"` → `"I"`，数组描述符 `"[I"`）。 */
  private val primitiveDescriptors = Map(
    "boolean" -> "Z", "byte" -> "B", "char" -> "C", "short" -> "S",
    "int" -> "I", "long" -> "J", "float" -> "F", "double" -> "D")

  private def merge(clazz: Class[_], p: AotPolicy): Unit = {
    typePolicies.get(clazz) match {
      case Some(existing) => typePolicies.update(clazz, existing.merge(p))
      case None           => typePolicies.put(clazz, p)
    }
  }

  private def isJdk(clazz: Class[_]): Boolean = {
    val name = clazz.getName
    jdkPrefixes.exists(name.startsWith)
  }

  /** Registers resource inclusion patterns (ant-style globs). */
  def registerPattern(patterns: String*): Unit = {
    val it = patterns.iterator
    while it.hasNext do this.patterns.add(it.next())
  }

  /** Registers a set of interfaces for JDK dynamic proxy (by class reference). */
  def registerProxy(interfaces: Class[_]*): Unit = {
    proxies.add(interfaces.toList.map(_.getName))
  }

  /** Registers a set of interfaces for JDK dynamic proxy by name.
   *
   *  用于无法直接引用（如包级私有/`private[core]`）的接口类，例如 Spring
   *  `SerializableTypeWrapper.SerializableTypeProxy`。接口顺序与运行期创建
   *  代理时一致（代理类按接口列表缓存）。
   */
  def registerProxyByName(interfaces: String*): Unit = {
    proxies.add(interfaces.toList)
  }

  /** Registers the no-arg constructor of a type by class name.
   *
   *  按类名定点登记无参构造器（生成官方格式的
   *  `"methods": [{ "name": "<init>", "parameterTypes": [] }]` 条目），供编译期无法
   *  `classOf` 引用、或会被 [[addType]] 的 JDK 前缀过滤吞掉的类型使用——典型场景是
   *  GraalVM native-image 的 JDK URL 协议 handler：运行期 URL 机制按
   *  `sun.net.www.protocol.<protocol>.Handler` 名字反射
   *  `getDeclaredConstructor().newInstance()`，类与无参构造器必须显式进镜像
   *  （见 beangle/build docs/graalvm-reachability-metadata.md）。显式点名登记即有意
   *  为之，不走 `isJdk` 过滤；类不存在时静默忽略（native-image 构建期会校验类型名）。
   */
  def registerConstructor(typeNames: String*): Unit = {
    val it = typeNames.iterator
    while it.hasNext do constructors.add(it.next())
  }

  /** Registers a type as reachable from JNI (native code `FindClass`).
   *
   *  按类名登记 JNI 可达类型（生成 GraalVM 25 `reflection` 条目的
   *  `"jniAccessible": true`）。JNI 元数据只能写在 `reachability-metadata.json`，
   *  且仅登记类型**不足以**让本机代码 `GetMethodID`/`GetFieldID` —— 方法/字段还需
   *  [[registerJniMethod]]/[[registerJniField]]（或按策略粗粒度展开，见
   *  [[AotPolicy.jniAccessible]]）。显式点名登记即有意为之，不走 `isJdk` 过滤，
   *  适用于 `classOf` 无法引用的 JDK 内部类（如 `sun.font.Font2D`）。
   */
  def registerJniType(typeNames: String*): Unit = {
    val it = typeNames.iterator
    while it.hasNext do jniTypes.add(it.next())
  }

  /** Registers a method of a JNI-accessible type (`GetMethodID`/`GetStaticMethodID`).
   *
   *  参数类型按 JSON 里的写法给出（`"int"`、`"char"`、`"java.lang.String"`、
   *  `"sun.java2d.loops.CompositeType"` …）；构造器用名字 `<init>`。示例：
   *  {{{
   *  hints.registerJniMethod("sun.font.Font2D", "charToGlyphRaw", "int")
   *  hints.registerJniMethod("sun.font.Font2D", "charToVariationGlyphRaw", "int", "int")
   *  hints.registerJniMethod("sun.font.Font2D", "getMapper")
   *  }}}
   *  登记即隐式包含该类型的 `jniAccessible`。
   */
  def registerJniMethod(typeName: String, methodName: String, parameterTypeNames: String*): Unit = {
    jniTypes.add(typeName)
    jniMethods.getOrElseUpdate(typeName, mutable.LinkedHashSet.empty)
      .add((methodName, parameterTypeNames.toList))
  }

  /** Registers a field of a JNI-accessible type (`GetFieldID`/`GetStaticFieldID`). */
  def registerJniField(typeName: String, fieldNames: String*): Unit = {
    jniTypes.add(typeName)
    jniFields.getOrElseUpdate(typeName, mutable.LinkedHashSet.empty).addAll(fieldNames)
  }

  /** Registers classes supporting Java serialization. */
  def registerSerializable(classes: Class[_]*): Unit = {
    val it = classes.iterator
    while it.hasNext do serializables.add(it.next())
  }

  /** Registers classes whose static initializers must run at runtime, not at
   *  native-image build time (e.g. SecureRandom users); emitted as
   *  `--initialize-at-run-time` in native-image.properties. */
  def registerRuntimeInitialized(classes: Class[_]*): Unit = {
    val it = classes.iterator
    while it.hasNext do runtimeInitialized.add(it.next())
  }

  /** Returns all registered reflection types. */
  def getTypes: collection.Set[Class[_]] = typePolicies.keySet

  /** Returns all registered reflection types with their policies. */
  def getTypePolicies: collection.Map[Class[_], AotPolicy] = typePolicies

  /** Returns all registered resource patterns. */
  def getPatterns: collection.Set[String] = patterns

  /** Returns all registered proxy interface sets (interface names, in order). */
  def getProxies: collection.Set[List[String]] = proxies

  /** Returns all class names with a registered no-arg constructor. */
  def getConstructors: collection.Set[String] = constructors

  /** Returns all class names registered as JNI-accessible. */
  def getJniTypes: collection.Set[String] = jniTypes

  /** Returns JNI methods per type as `(methodName, parameterTypeNames)`. */
  def getJniMethods: collection.Map[String, collection.Set[(String, List[String])]] = jniMethods

  /** Returns JNI fields per type. */
  def getJniFields: collection.Map[String, collection.Set[String]] = jniFields

  /** Returns all registered serializable classes. */
  def getSerializables: collection.Set[Class[_]] = serializables

  /** Returns all classes registered for runtime initialization. */
  def getRuntimeInitialized: collection.Set[Class[_]] = runtimeInitialized

  /** Returns true if no hints have been registered. */
  def isEmpty: Boolean =
    typePolicies.isEmpty && patterns.isEmpty && proxies.isEmpty && constructors.isEmpty &&
      jniTypes.isEmpty && serializables.isEmpty && runtimeInitialized.isEmpty

  /** Merges all hints from another [[AotHints]] into this one. */
  def addAll(other: AotHints): Unit = {
    other.typePolicies foreach { case (clazz, p) => merge(clazz, p) }
    patterns.addAll(other.patterns)
    proxies.addAll(other.proxies)
    constructors.addAll(other.constructors)
    other.jniTypes.foreach(jniTypes.add)
    other.jniMethods foreach { case (t, ms) =>
      val target = jniMethods.getOrElseUpdate(t, mutable.LinkedHashSet.empty)
      ms.foreach(target.add)
    }
    other.jniFields foreach { case (t, fs) =>
      val target = jniFields.getOrElseUpdate(t, mutable.LinkedHashSet.empty)
      fs.foreach(target.add)
    }
    serializables.addAll(other.serializables)
    runtimeInitialized.addAll(other.runtimeInitialized)
  }
}
