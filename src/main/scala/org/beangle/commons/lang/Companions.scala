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

package org.beangle.commons.lang

/** Scala companion object utilities. */
object Companions {

  /** Returns the companion object class for a Scala class.
   *
   * @param clazz the class (e.g. MyClass)
   * @return Some(MyClass$) or None
   */
  def getCompanionClass(clazz: Class[_]): Option[Class[_]] = {
    val clazzName = clazz.getName
    if clazzName.endsWith("$") then Some(clazz) else ClassLoaders.get(clazz.getName + "$")
  }

  /** Returns the companion object instance for a Scala class.
   *
   * @param clazz the class
   * @return Some(companion instance) or None
   */
  def getCompanion(clazz: Class[_]): Option[Any] = {
    getCompanionClass(clazz) match
      case None => None
      case Some(ct) => Option(ct.getDeclaredField("MODULE$").get(null))
  }
}
