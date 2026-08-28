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
  * （通常来自 [[AotHintRegistrar.aotPolicy]]，默认 public 方法 + public 构造器、无字段、
  * 不递归）；定制路径 `registerType(clazz, policy)` 对单个类显式指定策略。
  */
class AotHints(val policy: AotPolicy = AotPolicy.default) {

  private val typePolicies = mutable.LinkedHashMap.empty[Class[_], AotPolicy]
  private val patterns = Collections.newSet[String]
  private val proxies = Collections.newSet[List[Class[_]]]
  private val serializables = Collections.newSet[Class[_]]
  private val runtimeInitialized = Collections.newSet[Class[_]]

  /** Packages whose reflection metadata GraalVM already provides; skipped by
   *  the recursive hierarchy expansion in [[addType]]. */
  private val jdkPrefixes = Seq("java.", "javax.", "jdk.", "sun.", "com.sun.", "scala.")

  /** 简单路径：按容器默认策略（通常来自 registrar 的 `aotPolicy`）注册反射类型。 */
  def registerType(classes: Class[_]*): Unit = {
    val it = classes.iterator
    while it.hasNext do addType(it.next(), policy)
  }

  /** 定制路径：对单个类显式指定策略，例如 declared 成员、字段或递归父类。 */
  def registerType(clazz: Class[_], custom: AotPolicy): Unit = addType(clazz, custom)

  /** Adds a class with the given policy; when recursive, expands the non-JDK
   *  superclass and interface hierarchy with the same policy. */
  private def addType(clazz: Class[_], p: AotPolicy): Unit = {
    if (clazz == null || isJdk(clazz)) return
    val effective = if isEnumType(clazz) then p.merge(enumPolicy) else p
    merge(clazz, effective)
    // Scala 3 enum：应用只需注册枚举类型本身，伴生对象（`MODULE$` 单例入口）自动增量注册
    if classOf[scala.reflect.Enum].isAssignableFrom(clazz) then registerCompanion(clazz, effective)
    if effective.recursive then
      addType(clazz.getSuperclass, effective)
      clazz.getInterfaces foreach (addType(_, effective))
  }

  /** 按 `clazz.getName + "$"` 加载伴生类并注册（伴生实现 `Mirror.Sum`，同样命中
   *  [[isEnumType]] 自动补 public 字段）；伴生类缺失（罕见）时静默跳过。 */
  private def registerCompanion(clazz: Class[_], p: AotPolicy): Unit = {
    try {
      val companion = Class.forName(clazz.getName + "$", false, clazz.getClassLoader)
      if !companion.isInterface then addType(companion, p)
    } catch {
      case _: ClassNotFoundException | _: LinkageError => ()
    }
  }

  /** 枚举运行期需要读取字段：Scala 3 enum 经 `MODULE$` 取伴生单例、Java enum 经
   *  `$VALUES` 取常量，`EnumConverters`/`Enums`/`Reflections.getInstance` 都依赖字段
   *  反射。这些字段均为 public static（`MODULE$`/`$VALUES`），注册 enum 类型
   *  （含 Scala 3 枚举伴生 `Mirror.Sum`）时自动补 public 字段即可，避免每个应用
   *  为枚举伴生额外定制策略。 */
  private val enumPolicy = AotPolicy(Set(AotPolicy.Category.PublicFields))

  private def isEnumType(clazz: Class[_]): Boolean =
    clazz.isEnum || classOf[scala.reflect.Enum].isAssignableFrom(clazz) ||
      classOf[scala.deriving.Mirror.Sum].isAssignableFrom(clazz)

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

  /** Registers a set of interfaces for JDK dynamic proxy. */
  def registerProxy(interfaces: Class[_]*): Unit = {
    proxies.add(interfaces.toList)
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

  /** Returns all registered proxy interface sets. */
  def getProxies: collection.Set[List[Class[_]]] = proxies

  /** Returns all registered serializable classes. */
  def getSerializables: collection.Set[Class[_]] = serializables

  /** Returns all classes registered for runtime initialization. */
  def getRuntimeInitialized: collection.Set[Class[_]] = runtimeInitialized

  /** Returns true if no hints have been registered. */
  def isEmpty: Boolean =
    typePolicies.isEmpty && patterns.isEmpty && proxies.isEmpty && serializables.isEmpty && runtimeInitialized.isEmpty

  /** Merges all hints from another [[AotHints]] into this one. */
  def addAll(other: AotHints): Unit = {
    other.typePolicies foreach { case (clazz, p) => merge(clazz, p) }
    patterns.addAll(other.patterns)
    proxies.addAll(other.proxies)
    serializables.addAll(other.serializables)
    runtimeInitialized.addAll(other.runtimeInitialized)
  }
}
