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

package org.beangle.commons.text.inflector.rule

import org.beangle.commons.text.inflector.Rule

import java.util.regex.Pattern

/** Inflection rule by suffix replacement (e.g. -y → -ies).
 *
 * @author chaostone
 */
class SuffixInflectionRule(suffix: String, val singularSuffix: String, val pluralSuffix: String)
  extends Rule {

  private val regex = Pattern.compile("(?i).*" + suffix.substring(1) + "$")

  /** Alternate constructor: singular and plural suffixes. */
  def this(singularSuffix: String, pluralSuffix: String) = {
    this(singularSuffix, singularSuffix, pluralSuffix)
  }

  def applies(word: String): Boolean = regex.matcher(word).matches()

  def apply(word: String): String = {
    val i = word.lastIndexOf(singularSuffix.substring(1))
    word.substring(0, i) + pluralSuffix.substring(1)
  }
}
