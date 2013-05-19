/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
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
