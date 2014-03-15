/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.collection.page

object PageLimit {
  def apply(pageNo: Int, pageSize: Int) = new PageLimit(pageNo, pageSize)
}
/**
 * 查询分页限制
 *
 * @author chaostone
 */
class PageLimit(val pageNo: Int, val pageSize: Int) extends Limit {

  /**
   * <p>
   * isValid.
   * </p>
   *
   * @return a boolean.
   */
  def isValid(): Boolean = pageNo > 0 && pageSize > 0

  /**
   * <p>
   * toString.
   * </p>
   *
   * @see java.lang.Object#toString()
   * @return a {@link java.lang.String} object.
   */
  override def toString(): String = {
    new StringBuilder().append("pageNo:").append(pageNo)
      .append(" pageSize:")
      .append(pageSize)
      .toString
  }
}
