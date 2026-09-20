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

/** Runtime checks (notnull). */
object Checks {

  /** Throws IllegalArgumentException if obj is null.
   *
   * 注：原先标注 @elidable(ASSERTION)，但 Scala 3 不支持 elision，该注解一直是空操作，
   * 自 3.8.0 起被标记弃用（scala.annotation.elidable is not supported by Scala 3），故移除。
   *
   * @param obj the object to check
   */
  @inline
  def notnull(obj: AnyRef): Unit = {
    if (null == obj) throw new IllegalArgumentException("The argument can't be null")
  }
}
