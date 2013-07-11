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
package org.beangle.commons.bean.comparators

import java.text.Collator

/**
 * <p>
 * CollatorStringComparator class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class CollatorStringComparator(val asc: Boolean, val collator: Collator = Collator.getInstance) extends StringComparator {
  /**
   * <p>
   * compare.
   * </p>
   *
   * @param what0 a {@link java.lang.String} object.
   * @param what1 a {@link java.lang.String} object.
   * @return a int.
   */
  def compare(what0: String, what1: String): Int = {
    (if (asc) 1 else -1) *
      (collator.compare(if ((null == what0)) "" else what0, if ((null == what1)) "" else what1))
  }
}
