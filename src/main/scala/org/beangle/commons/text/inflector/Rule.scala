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

/** Pluralization/inflection rule (applies to word, returns transformed word).
 *
 * @author chaostone
 */
trait Rule {

  /** Returns true if this rule applies to the given word.
   *
   * @param word the word to test
   * @return true if this rule should be applied
   */
  def applies(word: String): Boolean

  /** Applies this rule to transform the word.
   *
   * @param word the word to transform
   * @return the transformed word
   */
  def apply(word: String): String
}
