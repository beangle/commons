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

import org.beangle.commons.cdi.Binder.Variable
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings

/** 带有占位符的属性描述
 */
object PlaceHolder {
  val Prefix: String = "${"
  val Suffix: String = "}"

  def hasVariable(pattern: String): Boolean = {
    val startIdx = pattern.indexOf(Prefix)
    if (startIdx > -1) {
      val endIdx = pattern.indexOf(Suffix, startIdx)
      endIdx - startIdx > 2
    } else {
      false
    }
  }

  def apply(pattern: String): PlaceHolder = {
    var n = pattern
    val variables = Collections.newSet[Variable]
    while
      val v = Strings.substringBetween(n, Prefix, Suffix)
      val hasVar = Strings.isNotBlank(v)
      if (hasVar) {
        variables.addOne(Variable(v))
        n = Strings.replace(n, Prefix + v + Suffix, "")
      }
      hasVar
    do {}
    PlaceHolder(pattern, variables.toSet)
  }
}

case class PlaceHolder(pattern: String, variables: Set[Variable])
