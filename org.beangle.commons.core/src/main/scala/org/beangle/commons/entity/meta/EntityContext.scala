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
package org.beangle.commons.entity.meta

import org.beangle.commons.entity.Entity
/**
 * <p>
 * EntityContext interface.
 * </p>
 * 
 * @author chaostone
 * @version $Id: $
 */
trait EntityContext {
  /**
   * 根据实体名查找实体类型
   * 
   * @param name a {@link java.lang.String} object.
   * @return a {@link org.beangle.commons.entity.metadata.Type} object.
   */
  def getType(name:String):Option[EntityType]

  def newInstance(name:String):Option[AnyRef] = getType(name) match{
    case Some(t) => Some(t.newInstance())
    case _ => None
  }

  def newInstance[T <: Entity[_]](entityClass:Class[T]):Option[T] = {
    val types= getTypes(entityClass)
    if(types.isEmpty) None
    else Some(types.head.newInstance().asInstanceOf[T])
  }

/*  def newInstance[T <: Entity[ID],ID](entityClass:Class[T],id:ID):Option[T] = {
    val types= getTypes(entityClass)
    val obj= if(types.isEmpty) null else types.head.newInstance()
    if(null!=obj) PropertyUtils.setProperty(entity, type.idName, id)
    if(null==obj) None else Some(obj)
  }*/
  /**
   * 根据类型,查找实体类型<br>
   * 找到实体名或者实体类名和指定类类名相同的entityType
   * 
   * @param entityClass a {@link java.lang.Class} object.
   * @return a {@link org.beangle.commons.entity.metadata.EntityType} object.
   */
  def getTypes(entityClass:Class[_]):List[EntityType]

}
