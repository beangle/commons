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
package org.beangle.commons.text.replace

import java.util.regex.Pattern

object Replacer {

  /**
   * process.
   *
   */
  def process(input: String, replacers: List[Replacer]): String = {
    if (null eq input) return null
    var in = input
    for (replacer <- replacers) in = replacer.process(in)
    in
  }
}

/**
 * Replace target with value on any input.
 *
 * @author chaostone
 * @version $Id: $
 */
class Replacer(key: String, var value: String) {

  var pattern: Pattern = Pattern.compile(key)

  var target: String = key

  /**
   * <p>
   * process.
   * </p>
   *
   * @param input a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  def process(input: String): String = {
    pattern.matcher(input).replaceAll(value)
  }

  /**
   * <p>
   * toString.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  override def toString(): String = target + "=" + value
}
