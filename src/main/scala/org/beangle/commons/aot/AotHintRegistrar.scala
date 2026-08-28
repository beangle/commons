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

  protected lazy val hints = new AotHints(aotPolicy)

  /** Returns the collected hints for config file generation. */
  final def aotHints: AotHints = hints
}
