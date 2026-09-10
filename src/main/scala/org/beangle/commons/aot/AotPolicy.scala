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

/** 反射注册策略：决定 [[AotHints.registerType]] 对一个类展开哪些成员。
 *
 * 类别只在"可见性"这一个维度上区分：`Public*` 依赖 GraalVM `allPublic*` 语义天然
 * 覆盖继承链（public 成员含父类，无需递归）；`Declared*` 仅本类声明，继承成员需配合
 * `recursive = true`。
 *
 * 不再区分"仅元数据查询（introspect-only）"与"可反射调用"：GraalVM 21 时代用
 * `queryAll*` 表达前者，但 GraalVM 25 的 reachability-metadata（v1.2.0）已删除该系列
 * 属性（schema 把 `all*` 定义为 "for reflective invocation"，登记即可查找并调用），
 * 且新格式里出现 `queryAll*` 会被 native-image 报 "Unknown attribute(s)" 后丢弃。
 * 因此登记即完整访问：可 `method.invoke`/`field.get`/`set`，也可 `getDeclaredMethods`
 * 等元数据查询。
 *
 * 默认 [[AotPolicy.default]]：public 方法 + public 构造器（可调用）、无字段、不递归，
 * 即 native 下 [[org.beangle.commons.bean.meta.MetaLoaderLite]] 所需的最小注册。
 * [[AotPolicy.bean]]：在默认基础上增加 declared 字段 + declared 方法、递归父类/接口，
 * 为运行时反射工具（如 [[org.beangle.commons.bean.meta.MetaLoader]]）设计。
 * [[AotPolicy.full]]：全部 public/declared 方法、构造器与字段（均可调用），递归父类/接口。
 */
object AotPolicy {

  /** 成员类别，只表达可见性（Public 含继承链 / Declared 仅本类声明）。
   *
   *  一一对应 GraalVM `allPublic*` / `allDeclared*` 批量标志。 */
  enum Category {
    case PublicMethods, DeclaredMethods
    case PublicConstructors, DeclaredConstructors
    case PublicFields, DeclaredFields
  }

  /** 默认安全策略：public 方法 + public 构造器，可调用；无字段；不递归。
   *
   * 兼顾可用性（public 成员可正常反射调用）与体积/性能（不递归、不登记
   * private/protected/字段元数据）。
   */
  val default: AotPolicy = {
    AotPolicy(Set(Category.PublicMethods, Category.PublicConstructors))
  }

  /** Bean 属性发现策略：在 [[default]] 基础上增加 declared 字段 + declared 方法，
   *  并递归展开父类/接口层级。
   *
   *  为 [[org.beangle.commons.bean.meta.MetaLoader]] 等运行时反射工具设计：
   *  - `DeclaredFields`：对应 GraalVM `allDeclaredFields`，支持 `getDeclaredFields` 读取字段名
   *    和修饰符。
   *  - `DeclaredMethods`：对应 GraalVM `allDeclaredMethods`，支持 `getDeclaredMethods` /
   *    `getModifiers` / `isAnnotationPresent` 等元数据查询（MetaLoader 不 invoke，
   *    但 GraalVM 25 已无 query-only 注册，登记即同时开放调用）。
   *  - `recursive = true`：`Declared*` 仅覆盖本类声明的成员，
   *    MetaLoader 需要遍历整个继承链（父类 + 接口）来收集全部字段和方法。
   */
  val bean: AotPolicy = {
    AotPolicy(Set(
      Category.PublicMethods, Category.PublicConstructors,
      Category.DeclaredFields, Category.DeclaredMethods
    ), recursive = true)
  }

  /** 全量注册策略：public/declared 的方法、构造器、字段全部可调用，并递归展开父类/接口。
   *
   *  对应 GraalVM `allPublic*` / `allDeclared*` 标志，适合需要完整反射访问
   *  （可 `invoke` / `get` / `set`）的场景：
   *  - `Declared*` 类别覆盖本类声明的非 public 成员；
   *  - `Public*` 类别天然覆盖继承链上的 public 成员；
   *  - `recursive = true` 让 `Declared*` 成员同样覆盖父类与接口层级。
   */
  val full: AotPolicy = {
    AotPolicy(Set(
      Category.PublicMethods, Category.DeclaredMethods,
      Category.PublicConstructors, Category.DeclaredConstructors,
      Category.PublicFields, Category.DeclaredFields
    ), recursive = true)
  }

  /** 枚举注册策略：在 [[bean]] 基础上保留 public 字段。
   *
   *  枚举运行期除了反射方法/构造器（`Enums` 的 `valueOf`/`values`/`id`、
   *  `BeanInfos` 的属性 getter），还要读 `MODULE$`/`$VALUES` 等 public 字段
   *  （`EnumConverters`/`Reflections` 取单例/常量），供 [[AotHints.registerEnum]]
   *  对枚举类、伴生对象与值类统一应用。 */
  val enumPolicy: AotPolicy = {
    bean.merge(AotPolicy(Set(Category.PublicFields)))
  }

  /** 数组类型注册策略：无成员类别，仅标记 unsafeAllocated（GraalVM 允许在镜像中
   *  分配该数组类型，供 `Array.newInstance` 使用）。 */
  val array: AotPolicy = {
    AotPolicy(Set.empty[Category], unsafeAllocated = true)
  }
}

/** 不可变的注册策略，描述 [[AotHints.registerType]] 对类的展开方式。
 *
 * 定制时手工构造该对象（比默认 `registerType(clazz)` 繁琐）：
 * {{{
 * AotPolicy(Set(Category.DeclaredMethods, Category.DeclaredConstructors,
 *               Category.DeclaredFields), recursive = true)
 * }}}
 *
 * `unsafeAllocated`：允许 `Unsafe.allocateInstance`/JNI `AllocObject` 无构造器实例化。
 * `jniAccessible`：把该类型连同其上登记的成员开放给 JNI（GraalVM 25 的
 * `reflection` 条目 `jniAccessible` 字段），供本机代码 `FindClass`/`GetMethodID`
 * 等反查；成员仍按 `categories` 展开（`allDeclared*`/`allPublic*` 对 JNI 同样生效）。
 * 需要精确成员清单（如 JDK 内部类、agent 采集的 C→Java 回调）时改用
 * [[AotHints.registerJniMethod]]/[[AotHints.registerJniField]]。
 */
final case class AotPolicy(
    categories: Set[AotPolicy.Category],
    recursive: Boolean = false,
    unsafeAllocated: Boolean = false,
    jniAccessible: Boolean = false) {

  /** 合并另一个策略：类别取并集，recursive/unsafeAllocated/jniAccessible 取或。 */
  def merge(other: AotPolicy): AotPolicy =
    AotPolicy(categories ++ other.categories, recursive || other.recursive,
      unsafeAllocated || other.unsafeAllocated, jniAccessible || other.jniAccessible)
}
