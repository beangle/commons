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

package org.beangle.commons.text.replace

import java.util.regex.Pattern

/** Replacer factory. */
object Replacer {

  /** Applies replacers in sequence to input. */
  def process(input: String, replacers: List[Replacer]): String = {
    if (null eq input) return null
    var in = input
    for (replacer <- replacers) in = replacer.process(in)
    in
  }
}

/** String replacement strategy. */
trait Replacer {
  /** Processes input and returns the result. */
  def process(input: String): String
}

/** Replaces regex matches with a fixed value. */
class PatternReplacer(key: String, var value: String) extends Replacer {

  /** Regex pattern for matching. */
  var pattern: Pattern = Pattern.compile(key)

  /** Display label for this replacer. */
  var target: String = key

  /** Replaces all pattern matches with value. */
  def process(input: String): String =
    pattern.matcher(input).replaceAll(value)

  override def toString: String = target + "=" + value
}
