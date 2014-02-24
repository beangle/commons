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
package org.beangle.commons.inject

import scala.collection.mutable

object Containers {

  var root: Container = _

  var hooks: List[ContainerHook] = Nil

  val subContainers = new mutable.HashMap[Long, Container]

  def getRoot(): Container = root

  def setRoot(root: Container) {
    Containers.root = root
  }

  def getHooks(): List[ContainerHook] = hooks

  def addHook(hook: ContainerHook) {
    hooks = hook :: hooks
  }

  def register(id: Long, container: Container) {
    subContainers.put(id, container)
  }

  def remove(id: Long) {
    subContainers.remove(id)
  }

  def get(id: Long): Container = subContainers(id)
}