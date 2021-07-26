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

package org.beangle.commons.bean.orderings

import java.text.Collator

/**
 * Collator Ordering
 * @author chaostone
 */
class CollatorOrdering(val asc: Boolean, val collator: Collator = Collator.getInstance) extends StringOrdering {
  /**
   * compare
   * @param what0 a String object.
   * @param what1 a String object.
   * @return a int.
   */
  def compare(what0: String, what1: String): Int =
    (if (asc) 1 else -1) *
      collator.compare(if (null == what0) "" else what0, if (null == what1) "" else what1)
}
