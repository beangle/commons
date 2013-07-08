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
package org.beangle.commons.jdbc.dialect

import org.beangle.commons.lang.Strings

class LimitGrammarBean(pattern: String, offsetPattern: String, bindInReverseOrder: Boolean,
                       bindFirst: Boolean, useMax: Boolean) extends LimitGrammar {

  def limit(query: String, hasOffset: Boolean) =
    if (hasOffset) Strings.replace(offsetPattern, "{}", query) else Strings.replace(pattern, "{}", query)

  /**
   * ANSI SQL defines the LIMIT clause to be in the form LIMIT offset, limit.
   * Does this dialect require us to bind the parameters in reverse order?
   *
   * @return true if the correct order is limit, offset
   */
  def isBindInReverseOrder = bindInReverseOrder

  def isBindFirst = bindFirst

  def isUseMax = useMax

}
