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

package org.beangle.commons.collection

/** Set using IdentityMap(not thread safe)
  *
  * @since 4.2.3
  */
class IdentitySet[A <: AnyRef] extends scala.collection.mutable.Set[A] {
  val map = new IdentityMap[A, A]

  override def iterator: Iterator[A] =
    map.keysIterator

  override def contains(elem: A): Boolean =
    map.contains(elem)

  override def subtractOne(elem: A): this.type = {
    map.remove(elem)
    this
  }

  override def addOne(elem: A): this.type = {
    map.put(elem, elem)
    this
  }

  override def clear(): Unit =
    map.clear()
}
