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

package org.beangle.commons.config

import org.beangle.commons.collection.Collections

/** Config backed by key-value map with prefix support. */
abstract class AbstractMapConfig extends Config {

  /** Returns iterator over all property keys. */
  def keysIterator: Iterator[String]

  final override def get(name: String, defaults: Any): Any = {
    val value = getValue(name, null)
    if (null == value) {
      val p = prefixOf(name)
      val i = keysIterator.filter(_.startsWith(p))
      val map = i.map(x => (x, getValue(x, ""))).toMap
      if map.isEmpty then defaults else wrap(map)
    } else {
      value
    }
  }

  final override def contains(name: String): Boolean = {
    val value = getValue(name, null)
    if (null == value) {
      val p = prefixOf(name)
      keysIterator.exists(_.startsWith(p))
    } else {
      true
    }
  }

  final override def keys(prefix: String): Iterable[String] = {
    val p = prefixOf(prefix)
    val i = keysIterator
    val ks = Collections.newBuffer[String]
    while (i.hasNext) {
      val k = i.next()
      if k.startsWith(p) then ks.addOne(k)
    }
    ks
  }
}
