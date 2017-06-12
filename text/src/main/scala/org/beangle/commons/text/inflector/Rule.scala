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
package org.beangle.commons.text.inflector

/**
 * Rule interface.
 *
 * @author chaostone
 */
trait Rule {

  /**
   * Tests to see if this rule applies for the given word.
   * @return <code>true</code> if this rule should be applied, <code>false</code> otherwise
   */
  def applies(word: String): Boolean

  /**
   * Applies this rule to the word, and transforming it into a new form.
   * @return the transformed word
   */
  def apply(word: String): String
}
