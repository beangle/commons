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

package org.beangle.commons.lang.testbean
import org.beangle.commons.lang.reflect.BeanInfos

import java.beans.{BeanInfo, Transient}

trait R{
  def title():String={
    "r title"
  }
}

trait E[A]

class Ap[T, V, S] extends R {
  var id: T = _
  var name: V = _
  var roleIds: collection.mutable.Buffer[S] = _
  var names: Set[String] = _
  var jobs: Map[String, S] = _
  var name2s: Set[E[T]] = _
  @Transient
  var persisted: Boolean=_

  def findById(id: T): Option[Ap[T, V, S]] = {
    None
  }
}

class AA (myId:Long=2) extends Ap[Long, String, Long]{
  def this(myIdString:String)={
    this(myIdString.toLong)
  }

  def processName(name:String):Unit={
    println("in process")
  }
  def usingMyId(m:Int=2,n:String="test"):Unit={
    println(myId)
  }

  override def findById(id: Long): Option[Ap[Long,String,Long]] = {
    None
  }
}

class TT(val name:String="x")
