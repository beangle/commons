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
package org.beangle.commons.bean.orderings

import scala.collection.mutable.ListBuffer
import org.beangle.commons.lang.Strings

/**
 * 多个属性的比较
 *
 * @author chaostone
 * @version $Id: $
 */
class MultiPropertyOrdering(propertyStr: String) extends Ordering[Any] {

  val chain = buildChainOrdering(propertyStr);

  def buildChainOrdering(propertyStr: String): ChainOrdering[Any] = {
    val properties = Strings.split(propertyStr, ',')
    val comparators = new ListBuffer[PropertyOrdering]
    properties.foreach(property => comparators += new PropertyOrdering(property.trim()))
    return new ChainOrdering(comparators.toList)
  }

  override def compare(arg0: Any, arg1: Any): Int = chain.compare(arg0, arg1)

}
