/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.text.inflector.rule

import java.util.regex.Pattern
import org.beangle.commons.text.inflector.Rule

/**
 * SuffixInflectionRule class.
 *
 * @author chaostone
 */
class SuffixInflectionRule(suffix: String, val singularSuffix: String, val pluralSuffix: String)
    extends Rule {

  private val regex = Pattern.compile("(?i).*" + suffix.substring(1) + "$")

  /**
   * Construct a rule for words with suffix <code>singularSuffix</code> which becomes
   * <code>pluralSuffix</code> in the plural.
   *
   * @param singularSuffix the singular suffix, starting with a "-" character
   * @param pluralSuffix the plural suffix, starting with a "-" character
   */
  def this(singularSuffix: String, pluralSuffix: String) {
    this(singularSuffix, singularSuffix, pluralSuffix)
  }

  def applies(word: String): Boolean = regex.matcher(word).matches()

  def apply(word: String): String = {
    val i = word.lastIndexOf(singularSuffix.substring(1))
    word.substring(0, i) + pluralSuffix.substring(1)
  }
}
