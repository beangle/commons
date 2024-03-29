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

import org.beangle.commons.lang.Strings

import scala.collection.mutable.ListBuffer

/** 多个属性的比较
  *
  * @author chaostone
  */
class MultiPropertyOrdering(propertyStr: String) extends Ordering[Any] {

  private val chain = buildChainOrdering(propertyStr)

  def buildChainOrdering(propertyStr: String): ChainOrdering[Any] = {
    val properties = Strings.split(propertyStr, ',')
    val comparators = new ListBuffer[PropertyOrdering]
    properties.foreach(property => comparators += new PropertyOrdering(property.trim()))
    new ChainOrdering(comparators.toList)
  }

  override def compare(arg0: Any, arg1: Any): Int = chain.compare(arg0, arg1)
}
