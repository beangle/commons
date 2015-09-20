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
package org.beangle.commons.text.inflector

import java.util.Collections
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import RuleBasedPluralizer._

object RuleBasedPluralizer {

  class IdentityPluralizer extends Pluralizer {

    def pluralize(word: String): String = word

    def pluralize(word: String, number: Int): String = word
  }

  private val IDENTITY_PLURALIZER = new IdentityPluralizer()

  private var pattern: Pattern = Pattern.compile("\\A(\\s*)(.+?)(\\s*)\\Z")

  private var pluPattern1: Pattern = Pattern.compile("^\\p{Lu}+$")

  private var pluPattern2: Pattern = Pattern.compile("^\\p{Lu}.*")
}

/**
 * RuleBasedPluralizer class.
 *
 * @author chaostone
 */
class RuleBasedPluralizer(var rules: List[Rule], var locale: Locale, var fallbackPluralizer: Pluralizer)
    extends Pluralizer {

  /**
   * Constructs a pluralizer with an empty list of rules. Use the setters to configure.
   */
  def this() {
    this(List.empty, Locale.getDefault, null)
  }

  /**
   * Constructs a pluralizer that uses a list of rules then an identity {@link Pluralizer} if none
   * of the rules match. This is useful to build your own {@link Pluralizer} from scratch.
   *
   * @param rules  the rules to apply in order
   * @param locale the locale specifying the language of the pluralizer
   */
  def this(rules: List[Rule], locale: Locale) {
    this(rules, locale, IDENTITY_PLURALIZER)
  }

  /**
   * Converts a noun or pronoun to its plural form.
   * This method is equivalent to calling <code>pluralize(word, 2)</code>.
   * The return value is not defined if this method is passed a plural form.
   */
  def pluralize(word: String): String = pluralize(word, 2)

  /**
   * Converts a noun or pronoun to its plural form for the given number of instances. If
   * <code>number</code> is 1, <code>word</code> is returned unchanged.
   * The return value is not defined if this method is passed a plural form.
   */
  def pluralize(word: String, number: Int): String = {
    if (number == 1) {
      return word
    }
    val matcher = pattern.matcher(word)
    if (matcher.matches()) {
      val pre = matcher.group(1)
      val trimmedWord = matcher.group(2)
      val post = matcher.group(3)
      val plural = pluralizeInternal(trimmedWord)
      if (plural == null) {
        return fallbackPluralizer.pluralize(word, number)
      }
      return pre + postProcess(trimmedWord, plural) + post
    }
    word
  }

  /**
   * Goes through the rules in turn until a match is found at which point the rule is applied and
   * the result returned. If no rule matches, returns <code>null</code>.
   *
   * @param word a singular noun
   * @return the plural form of the noun, or <code>null</code> if no rule matches
   */
  protected def pluralizeInternal(word: String): String = {
    rules.find(_.applies(word)).map(_.apply(word)).getOrElse(null)
  }

  /**
   * <p>
   * Apply processing to <code>pluralizedWord</code>. This implementation ensures the case of the
   * plural is consistent with the case of the input word.
   * </p>
   * <p>
   * If <code>trimmedWord</code> is all uppercase, then <code>pluralizedWord</code> is uppercased.
   * If <code>trimmedWord</code> is titlecase, then <code>pluralizedWord</code> is titlecased.
   * </p>
   *
   * @param trimmedWord the input word, with leading and trailing whitespace removed
   * @param pluralizedWord the pluralized word
   * @return the <code>pluralizedWord</code> after processing
   */
  protected def postProcess(trimmedWord: String, pluralizedWord: String): String = {
    if (pluPattern1.matcher(trimmedWord).matches()) {
      return pluralizedWord.toUpperCase(locale)
    } else if (pluPattern2.matcher(trimmedWord).matches()) {
      return pluralizedWord.substring(0, 1).toUpperCase(locale) + pluralizedWord.substring(1)
    }
    pluralizedWord
  }
}
