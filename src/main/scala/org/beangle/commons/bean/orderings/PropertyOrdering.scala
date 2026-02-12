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

package org.beangle.commons.bean.orderings

import org.beangle.commons.bean.Properties
import org.beangle.commons.lang.{Numbers, Strings}

import scala.collection.mutable.ListBuffer

/** PropertyOrdering factory. */
object PropertyOrdering {

  /** Creates Ordering from property string (e.g. "name", "name,age" for ChainOrdering). */
  def by(cmpStr: String): Ordering[Any] = {
    if cmpStr.contains(",") then
      val properties = Strings.split(cmpStr, ',')
      val comparators = new ListBuffer[PropertyOrdering]
      properties.foreach(property => comparators += new PropertyOrdering(property.trim()))
      new ChainOrdering(comparators.toList)
    else
      new PropertyOrdering(cmpStr)
  }
}

/** Property comparator for ordering objects by property values.
 *
 * @author chaostone
 */
class PropertyOrdering(cmpStr: String) extends Ordering[Any] {

  /** Property name to compare. */
  var name: String = cmpStr.trim()

  private var index: Int = -1

  /** True for ascending order. */
  var asc: Boolean = true

  /** True to treat nulls as smallest. */
  var nullFirst: Boolean = true

  /** Custom comparator for the extracted value. */
  var comparator: Ordering[Any] = _

  /** Collator for string comparison. */
  var collatorOrdering = new CollatorOrdering(asc)

  require(Strings.isNotEmpty(cmpStr))

  if (Strings.contains(cmpStr, ','))
    throw new RuntimeException("PropertyOrdering don't support comma based order by. Use MultiPropertyOrdering instead.")

  if ('[' == name.charAt(0)) {
    index = Numbers.toInt(Strings.substringBetween(name, "[", "]"))
    name = Strings.substringAfter(name, "]")
    if (name.length > 0 && '.' == name.charAt(0))
      name = name.substring(1)
  }

  if (Strings.contains(name, ' ')) {
    if (Strings.contains(name, " desc")) asc = false
    name = name.substring(0, name.indexOf(' '))
  }

  /** Creates PropertyOrdering with explicit asc flag. */
  def this(cmpWhat: String, asc: Boolean) = {
    this(cmpWhat + " " + (if (asc) "" else "desc"))
  }

  def compare(arg0: Any, arg1: Any): Int = {
    val first = extractCompareValue(arg0)
    val second = extractCompareValue(arg1)
    if (first == null && null == second) return 0

    if (null == comparator) {
      if (first == null && null != second) return if (asc && nullFirst) -1 else 1

      if (first != null && null == second) return if (asc && nullFirst) 1 else -1

      if (first.isInstanceOf[String] || second.isInstanceOf[String])
        collatorOrdering.compare(first.toString, second.toString)
      else if (asc)
        first.asInstanceOf[Comparable[Any]].compareTo(second)
      else
        second.asInstanceOf[Comparable[Any]].compareTo(first)
    } else
      comparator.compare(first, second)
  }

  private def extractCompareValue(a: Any): Any = {
    var obj: Any = a
    if index > -1 then obj = obj.asInstanceOf[Array[Any]](index)
    if Strings.isNotEmpty(name) then obj = Properties.get[Any](obj, name)

    obj match
      case null => null
      case Some(v) => v
      case None => null
      case _ => obj
  }
}
