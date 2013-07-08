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
package org.beangle.commons.entity

import org.beangle.commons.entity.util.ValidKey

trait Entity[ID] extends Serializable{

  /**
   * Return Identifier
   */
   def id: ID

  /**
   * Return true if persisted
   */
 def persisted:Boolean = ValidKey(id)

  /**
   * @see java.lang.Object#hashCode()
   */
  override def hashCode:Int = if(null == id)  629 else id.hashCode()

  /**
   * <p>
   * 比较id,如果任一方id是null,则不相等
   * </p>
   * 由于业务对象被CGlib或者javassist增强的原因，这里只提供一般的基于id的比较,不提供基于Class的比较。<br>
   * 如果在存在继承结构， 请重置equals方法。
   * 
   * @see java.lang.Object#equals(Object)
   */
   override def equals(other: Any):Boolean = other match {
     case that: Entity[_] => (this eq that) || (null != id && null!= that.id && id==that.id)
     case _ => false
   }
}

