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

import java.util.Map

import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.Entity
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

object AbstractEntityContext{

  def buildClassEntities(entityTypes:Map[String, EntityType]):Map[Class[_],List[EntityType]]={
    val builder=new collection.mutable.HashMap[Class[_],ListBuffer[EntityType]]
    for((k,v) <- entityTypes){
      var list=builder.get(v.entityClass).orNull
      if(null==list){
        list=new ListBuffer[EntityType]
        builder.put(v.entityClass,list)
      }
      list+=v
    }
    val rs=new collection.mutable.HashMap[Class[_],List[EntityType]]
    for((k,v) <- builder){
      rs.put(k,v.toList)
    }
    rs
  }
}

/**
 * <p>
 * AbstractEntityContext class.
 * </p>
 * 
 * @author chaostone
 * @version $Id: $
 */
abstract class AbstractEntityContext(val entityTypes:Map[String, EntityType]) extends EntityContext {

  val classEntities = AbstractEntityContext.buildClassEntities(entityTypes)

  override def getType(name:String):Option[EntityType] = {
    val t =  entityTypes.get(name)
    if(null==t) None else Some(t)
  }

  override def getTypes(entityClass :Class[_]):List[EntityType] = {
    val types =  classEntities.get(entityClass)
    if(null==types || types.isEmpty) Nil
    else types
  }
}
