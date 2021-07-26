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

package org.beangle.commons.collection.page

object PageLimit {
  def apply(pageIndex: Int, pageSize: Int) = new PageLimit(pageIndex, pageSize)
}
/**
 * 查询分页限制
 *
 * @author chaostone
 */
class PageLimit(val pageIndex: Int, val pageSize: Int) extends Limit {

  /**
   * <p>
   * isValid.
   * </p>
   *
   * @return a boolean.
   */
  def isValid(): Boolean = pageIndex > 0 && pageSize > 0

  /**
   * <p>
   * toString.
   * </p>
   *
   * @see java.lang.Object#toString()
   * @return a String object.
   */
  override def toString(): String =
    new StringBuilder().append("pageIndex:").append(pageIndex)
      .append(" pageSize:")
      .append(pageSize)
      .toString
}
