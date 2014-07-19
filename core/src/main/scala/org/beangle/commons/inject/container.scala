/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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

/**
 * Bean Container.
 *
 * @author chaostone
 * @since 3.1.0
 */
trait Container {

  def contains(key: Any): Boolean

  def getType(key: Any): Option[Class[_]]

  def getBean[T](key: Any): Option[T]

  def getBean[T](clazz: Class[T]): Option[T]

  def getBeans[T](clazz: Class[T]): Map[_, T]

  def keys(): Set[_]
}


object Containers {

  var root: Container = _

  var hooks: List[ContainerHook] = Nil

  val subContainers = new collection.mutable.HashMap[Long, Container]

  def getHooks(): List[ContainerHook] = hooks

  def addHook(hook: ContainerHook): Unit = hooks = hook :: hooks

  def register(id: Long, container: Container): Unit = subContainers.put(id, container)

  def remove(id: Long): Unit = subContainers.remove(id)

  def get(id: Long): Container = subContainers(id)
}


trait ContainerAware {

  def container: Container
  
  def container_=(container: Container): Unit
}

trait ContainerHook {

  def notify(context: Container): Unit
}

trait PropertySource {

  def properties: collection.Map[String, String]
}

