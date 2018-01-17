/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.bean.orderings

import org.beangle.commons.bean.Properties
import org.beangle.commons.lang.{Numbers, Strings}

/**
 * 属性比较器。<br>
 *
 * @author chaostone
 */
class PropertyOrdering(cmpStr: String) extends Ordering[Any] {

  var name: String = cmpStr.trim()

  private var index: Int = -1

  var asc: Boolean = true

  var nullFirst: Boolean = true

  var comparator: Ordering[Any] = _

  var collatorOrdering = new CollatorOrdering(asc)

  assert(Strings.isNotEmpty(cmpStr))

  if (Strings.contains(cmpStr, ',')) {
    throw new RuntimeException("PropertyOrdering don't support comma based order by. Use MultiPropertyOrdering instead.")
  }

  if ('[' == name.charAt(0)) {
    index = Numbers.toInt(Strings.substringBetween(name, "[", "]"))
    name = Strings.substringAfter(name, "]")
    if (name.length > 0 && '.' == name.charAt(0)) {
      name = name.substring(1)
    }
  }

  if (Strings.contains(name, ' ')) {
    if (Strings.contains(name, " desc")) asc = false
    name = name.substring(0, name.indexOf(' '))
  }

  def this(cmpWhat: String, asc: Boolean) {
    this(cmpWhat + " " + (if (asc) "" else "desc"))
  }

  def compare(arg0: Any, arg1: Any): Int = {
    var first = arg0
    var second = arg1
    if (index > -1) {
      first = first.asInstanceOf[Array[Any]](index)
      second = first.asInstanceOf[Array[Any]](index)
    }
    if (Strings.isNotEmpty(name)) {
      first = Properties.get[Any](first, name)
      second = Properties.get[Any](second, name)
    }
    if (first == null && null == second) return 0

    if (null == comparator) {
      if (first == null && null != second) return if (asc && nullFirst) -1 else 1

      if (first != null && null == second) return if (asc && nullFirst) 1 else -1

      if (first.isInstanceOf[String] || second.isInstanceOf[String]) {
        collatorOrdering.compare(first.toString, second.toString)
      } else {
        if (asc) {
          first.asInstanceOf[Comparable[Any]].compareTo(second)
        } else {
          second.asInstanceOf[Comparable[Any]].compareTo(first)
        }
      }
    } else {
      comparator.compare(first, second)
    }
  }
}
