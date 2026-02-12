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

package org.beangle.commons.file.text

import org.beangle.commons.lang.Strings

/** Processes a single line of text. */
trait LineProcessor {
  /** Processes the line and returns the result. */
  def process(line: String): String
}

/** Replaces tabs with spaces. */
class Tab2Space(tablength: Int = 2) extends LineProcessor {
  private val spaces = " " * tablength

  override def process(line: String): String =
    Strings.replace(line, "\t", spaces)
}

/** Trims trailing whitespace from lines. */
object TrimTrailingWhiteSpace extends LineProcessor {

  override def process(line: String): String =
    Strings.trimEnd(line)
}
