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

/** 反射注册策略：决定 [[AotHints.registerType]] 对一个类展开哪些成员、以何种访问深度。
 *
 * 两个正交维度编码在 [[Category]] 的命名中：
 *  - 可见性：`Public*` 依赖 GraalVM `allPublic*`/`queryAllPublic*` 语义天然覆盖继承链
 *    （public 成员含父类，无需递归）；`Declared*` 仅本类声明，继承成员需配合
 *    `recursive = true`。
 *  - 访问深度：无 `Query` 前缀的类别（如 `PublicMethods`）对应 GraalVM `all*` 标志，
 *    登记完整访问（可 `method.invoke`/`field.get`/`set`）；`Query*` 前缀对应
 *    `queryAll*` 标志，只登记元数据（可 `getMethod`/`getAnnotation`，不可 invoke），
 *    镜像更小，但运行时反射调用会失败。
 *
 * 默认 [[AotPolicy.default]]：public 方法 + public 构造器（可调用）、无字段、不递归。
 */
object AotPolicy {

  /** 成员类别，与 reflect-config.json 的标志一一对应；命名含可见性（Public/Declared）
   *  与访问深度（Query* 前缀为 introspect-only）两个维度。 */
  enum Category {
    case PublicMethods, DeclaredMethods
    case PublicConstructors, DeclaredConstructors
    case PublicFields, DeclaredFields
    case QueryPublicMethods, QueryDeclaredMethods
    case QueryPublicConstructors, QueryDeclaredConstructors
  }

  /** 默认安全策略：public 方法 + public 构造器，可调用；无字段；不递归。
   *
   * 兼顾可用性（public 成员可正常反射调用）与体积/性能（不递归、不登记
   * private/protected/字段元数据）。
   */
  val default: AotPolicy =
    AotPolicy(Set(Category.PublicMethods, Category.PublicConstructors))
}

/** 不可变的注册策略，描述 [[AotHints.registerType]] 对类的展开方式。
 *
 * 定制时手工构造该对象（比默认 `registerType(clazz)` 繁琐）：
 * {{{
 * AotPolicy(Set(Category.DeclaredMethods, Category.DeclaredConstructors,
 *               Category.DeclaredFields), recursive = true)
 * }}}
 */
final case class AotPolicy(
    categories: Set[AotPolicy.Category],
    recursive: Boolean = false,
    unsafeAllocated: Boolean = false) {

  /** 合并另一个策略：类别取并集，recursive/unsafeAllocated 取或。 */
  def merge(other: AotPolicy): AotPolicy =
    AotPolicy(categories ++ other.categories, recursive || other.recursive, unsafeAllocated || other.unsafeAllocated)
}
