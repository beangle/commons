/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.entity.meta

import org.beangle.commons.entity.Entity
import org.beangle.commons.bean.PropertyUtils
/**
 * <p>
 * EntityContext interface.
 * </p>
 *
 * @author chaostone
 */
trait EntityContext {
  /**
   * 根据实体名查找实体类型
   *
   * @param name a {@link java.lang.String} object.
   * @return a {@link org.beangle.commons.entity.metadata.Type} object.
   */
  def getType(clazz: Class[_]): Option[EntityType]

  def newInstance[T <: Entity[_]](entityClass: Class[T]): Option[T] = {
    getType(entityClass) match {
      case Some(t) => Some(t.newInstance().asInstanceOf[T])
      case _ => None
    }
  }

  def newInstance[T <: Entity[ID], ID](entityClass: Class[T], id: ID): Option[T] = {
    getType(entityClass) match {
      case Some(t) => {
        val obj = t.newInstance()
        PropertyUtils.setProperty(obj, t.idName, id)
        Some(obj.asInstanceOf[T])
      }
      case _ => None
    }
  }

}
