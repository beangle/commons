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

/** Property descriptor with placeholder support (e.g. ${...}). */
object PlaceHolder {
  /** Placeholder prefix, e.g. `${`. */
  val Prefix: String = "${"
  /** Placeholder suffix, e.g. `}`. */
  val Suffix: String = "}"

  /** Returns true if the pattern contains ${...} placeholders.
   *
   * @param pattern the string to check
   * @return true if contains variables
   */
  def hasVariable(pattern: String): Boolean = {
    val startIdx = pattern.indexOf(Prefix)
    if (startIdx > -1) {
      val endIdx = pattern.indexOf(Suffix, startIdx)
      endIdx - startIdx > 2
    } else {
      false
    }
  }

  /** Parses the pattern and extracts variable names from ${var} placeholders.
   *
   * @param pattern the pattern string
   * @return PlaceHolder with variables
   */
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

/** Parsed placeholder pattern with variable names and optional defaults.
 *
 * @param pattern   the original string with ${var} placeholders
 * @param variables the extracted variable descriptors
 */
case class PlaceHolder(pattern: String, variables: Set[Variable])
