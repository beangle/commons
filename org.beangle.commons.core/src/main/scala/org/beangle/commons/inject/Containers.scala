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
package org.beangle.commons.inject

import java.util.ArrayList
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
//remove if not needed
import scala.collection.JavaConversions._

object Containers {

  var root: Container = _

  val hooks = new ArrayList[ContainerHook]()

  var subContainers: Map[Long, Container] = CollectUtils.newHashMap()

  def getRoot(): Container = root

  def setRoot(root: Container) {
    Containers.root = root
  }

  def getHooks(): List[ContainerHook] = hooks

  def register(id: java.lang.Long, container: Container) {
    subContainers.put(id, container)
  }

  def remove(id: java.lang.Long) {
    subContainers.remove(id)
  }

  def get(id: java.lang.Long): Container = subContainers.get(id)
}
