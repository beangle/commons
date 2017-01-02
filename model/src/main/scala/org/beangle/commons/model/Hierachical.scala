/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.model

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Numbers
/**
 * <p>
 * Hierarchical interface.
 * </p>
 *
 * @author chaostone
 */
trait Hierarchical[T] extends Ordered[T] {

  /** index no */
  var indexno: String = _

  /** 父级菜单 */
  var parent: Option[T] = None

  var children = Collections.newBuffer[T]

  def depth: Int = {
    Strings.count(indexno, ".") + 1
  }

  def lastindex: Int = {
    var index = Strings.substringAfterLast(indexno, ".")
    if (Strings.isEmpty(index)) index = indexno
    var idx = Numbers.toInt(index)
    if (idx <= 0) idx = 1
    idx
  }

  def compare(that: T): Int = {
    this.indexno.compareTo(that.asInstanceOf[Hierarchical[_]].indexno)
  }
}
