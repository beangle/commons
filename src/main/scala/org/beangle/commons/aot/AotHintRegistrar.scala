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
 * [[org.beangle.commons.bean.meta.MetaRegistrar]] extends this trait, adding
 * compile-time BeanMeta registration via `register` macro. Its `registering()`
 * bridges metamodel classes into [[hints]] automatically.
 */
abstract class AotHintRegistrar extends Registrar {

  protected val hints = new AotHints

  /** Returns the collected hints for config file generation. */
  final def aotHints: AotHints = hints
}
