/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.bean.comparators

import java.util.Comparator

import scala.beans.BeanProperty
import scala.beans.BooleanBeanProperty
import scala.collection.JavaConversions._

import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings

/**
 * 属性比较器。<br>
 *
 * @author chaostone
 * @version $Id: $
 */
class PropertyComparator(cmpStr: String) extends Comparator[Any] {

  @BeanProperty
  var name: String = cmpStr.trim()

  private var index: Int = -1

  @BooleanBeanProperty
  var asc: Boolean = true

  @BooleanBeanProperty
  var nullFirst: Boolean = true

  @BeanProperty
  var comparator: Comparator[Any] = _

  @BeanProperty
  var stringComparator: StringComparator = new CollatorStringComparator(asc)

  assert(Strings.isNotEmpty(cmpStr))

  if (Strings.contains(cmpStr, ',')) {
    throw new RuntimeException("PropertyComparator don't support comma based order by. Use MultiPropertyComparator instead.")
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

  /**
   * <p>
   * Constructor for PropertyComparator.
   * </p>
   *
   * @param cmpWhat a {@link java.lang.String} object.
   * @param asc a boolean.
   */
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
      first = PropertyUtils.getProperty[Any](first, name)
      second = PropertyUtils.getProperty[Any](second, name)
    }
    if (first == null && null == second) return 0

    if (null == comparator) {
      if (first == null && null != second) return if (asc && nullFirst) -1 else 1

      if (first != null && null == second) return if (asc && nullFirst) 1 else -1

      if (first.isInstanceOf[String] || second.isInstanceOf[String]) {
        stringComparator.compare(first.toString, second.toString)
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
