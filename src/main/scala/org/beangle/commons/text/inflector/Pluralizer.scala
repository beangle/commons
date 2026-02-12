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

package org.beangle.commons.text.inflector

/** `Pluralizer` converts singular word forms to their plural forms.
 *
 * @author chaostone
 */
trait Pluralizer {

  /** Returns the plural form of the word.
   *
   * @param word the singular word
   * @return the plural form
   */
  def pluralize(word: String): String

  /** Returns plural or singular form based on count.
   *
   * @param word   the base word (singular)
   * @param number the count (1 = singular, else plural)
   * @return the appropriately inflected form
   */
  def pluralize(word: String, number: Int): String
}
