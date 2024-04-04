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

import java.util.regex.{Matcher, Pattern}

object AbstractRegexReplacementRule {

  /** Disjunction
    * <p>
    * Form the disjunction of the given regular expression patterns. For example if patterns contains
    * "a" and "b" then the disjunction is "(a|b)", that is, "a or b".
    * </p>
    *
    * @param patterns an array of regular expression patterns
    * @return a pattern that matches if any of the input patterns match
    */
  def disjunction(patterns: Array[String]): String = {
    var regex = ""
    for (i <- 0 until patterns.length) {
      regex += patterns(i)
      if (i < patterns.length - 1)
        regex += "|"
    }
    "(?:" + regex + ")"
  }

  /** disjunction
    * <p>
    * Form the disjunction of the given regular expression patterns. For example if patterns contains
    * "a" and "b" then the disjunction is "(a|b)", that is, "a or b".
    * </p>
    *
    * @param patterns a set of regular expression patterns
    * @return a pattern that matches if any of the input patterns match
    */
  def disjunction(patterns: Set[String]): String =
    disjunction(patterns.toArray)
}

import org.beangle.commons.text.inflector.rule.AbstractRegexReplacementRule.*

/** Abstract AbstractRegexReplacementRule class.
  *
  * @author chaostone
  */
abstract class AbstractRegexReplacementRule(regex: String) extends Rule {

  private val pattern = Pattern.compile(regex)

  def applies(word: String): Boolean = pattern.matcher(word).matches()

  def apply(word: String): String = {
    val matcher = pattern.matcher(word)
    if (!matcher.matches())
      throw new IllegalArgumentException("Word '" + word + "' does not match regex: " + pattern.pattern())
    replace(matcher)
  }

  /** Use the state in the given {@link Matcher} to perform a replacement.
    */
  def replace(matcher: Matcher): String
}
