/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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

import java.util.regex.Matcher
import java.util.regex.Pattern
import org.beangle.commons.text.inflector.Rule

object AbstractRegexReplacementRule {

  /**
   * <p>
   * Form the disjunction of the given regular expression patterns. For example if patterns contains
   * "a" and "b" then the disjunction is "(a|b)", that is, "a or b".
   * </p>
   *
   * @param patterns
   *          an array of regular expression patterns
   * @return a pattern that matches if any of the input patterns match
   */
  def disjunction(patterns: Array[String]): String = {
    var regex = ""
    for (i <- 0 until patterns.length) {
      regex += patterns(i)
      if (i < patterns.length - 1) {
        regex += "|"
      }
    }
    "(?:" + regex + ")"
  }

  /**
   * <p>
   * Form the disjunction of the given regular expression patterns. For example if patterns contains
   * "a" and "b" then the disjunction is "(a|b)", that is, "a or b".
   * </p>
   *
   * @param patterns
   *          a set of regular expression patterns
   * @return a pattern that matches if any of the input patterns match
   */
  def disjunction(patterns: Set[String]): String = {
    disjunction(patterns.toArray)
  }
}

import AbstractRegexReplacementRule._
/**
 * <p>
 * Abstract AbstractRegexReplacementRule class.
 * </p>
 *
 * @author chaostone
 */
abstract class AbstractRegexReplacementRule(regex: String) extends Rule {

  private val pattern = Pattern.compile(regex)

  /**
   * {@inheritDoc}
   */
  def applies(word: String): Boolean = pattern.matcher(word).matches()

  /**
   * {@inheritDoc}
   */
  def apply(word: String): String = {
    val matcher = pattern.matcher(word)
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Word '" + word + "' does not match regex: " + pattern.pattern())
    }
    replace(matcher)
  }

  /**
   * <p>
   * Use the state in the given {@link Matcher} to perform a replacement.
   * </p>
   *
   * @param matcher
   *          the matcher used to match the word
   * @return the transformed word
   */
  def replace(matcher: Matcher): String
}
