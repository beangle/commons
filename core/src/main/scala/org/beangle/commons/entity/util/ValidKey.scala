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
package org.beangle.commons.entity.util

import org.beangle.commons.lang.functor.NotEmpty;
import org.beangle.commons.lang.functor.NotZero
import org.beangle.commons.lang.functor.Predicate;

/**
 * 判断实体类中的主键是否是有效主键
 * 
 * @author chaostone
 */
object ValidKey extends Predicate[Any] {

  def apply(value: Any):Boolean = {
    if (value.isInstanceOf[AnyRef] && null == value) return false
    if (value.isInstanceOf[Number]) return  NotZero(value.asInstanceOf[Number])
    return NotEmpty.apply(value.toString)
  }

}
