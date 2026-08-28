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

import org.beangle.commons.lang.Registrar

/** Registers ahead-of-time hints for GraalVM native-image.
 *
 * Implementations declare reflection types, resource patterns, proxy interfaces,
 * and serializable classes by calling methods on [[hints]]. The collected hints
 * are exposed via [[aotHints]] for use by [[AotHintGenerator]].
 *
 * {{{
 * class MyHints extends AotHintRegistrar {
 *   override def registering(): Unit = {
 *     hints.registerType(classOf[User], classOf[Role])
 *     hints.registerPattern("META-INF/custom.idx")
 *     hints.registerProxy(classOf[UserService])
 *     hints.registerSerializable(classOf[UserDto])
 *   }
 * }
 * }}}
 *
 * [[hints]] 使用 [[aotPolicy]] 作为默认注册策略（默认 public 方法 + public 构造器、
 * 无字段、不递归）。绝大多数 registrar 无需改动；需要放宽（declared 成员、字段、
 * 递归父类）或收紧（仅 introspection）时覆写 [[aotPolicy]]，或对个别类走
 * `hints.registerType(clazz, customPolicy)` 显式定制。
 *
 * [[org.beangle.commons.bean.meta.MetaRegistrar]] extends this trait, adding
 * compile-time BeanMeta registration via the `register` macro; every `register`
 * call also adds the type to [[hints]].
 */
abstract class AotHintRegistrar extends Registrar {

  /** 本 registrar 的默认注册策略；覆写可整体调整 `registerType` 的展开方式。 */
  protected def aotPolicy: AotPolicy = AotPolicy.default

  /** 收集注册的容器，必须保持单一实例（注册会累积），故不能是 `def`。
   *
   * 用 `lazy` 而非 `val`：`aotPolicy` 是可覆写的开放方法，若在基类构造期间
   * 求值会动态分派到子类覆写，而此时子类字段尚未初始化（可能读到 null 或
   * 默认值）。`lazy` 将求值推迟到首次真正访问（`registering()`/`aotHints`），
   * 此时对象已完整构造，子类覆写引用任何状态都安全。
   */
  protected lazy val hints = new AotHints(aotPolicy)

  /** Returns the collected hints for config file generation. */
  final def aotHints: AotHints = hints
}
