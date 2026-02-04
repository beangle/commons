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

package org.beangle.commons.cdi

import org.beangle.commons.concurrent.Locks

import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Bean Container.
 *
 * @author chaostone
 * @since 3.1.0
 */
trait Container {

  def id: String

  def contains(key: String): Boolean

  def getType(key: String): Option[Class[_]]

  def getBean[T](key: String): Option[T]

  def getBean[T](clazz: Class[T]): Option[T]

  def getBeans[T](clazz: Class[T]): Map[String, T]

  def beanTypes: collection.Map[String, Class[_]]

  def close(): Unit
}

object Container {

  private var Default: Option[Container] = None
  private var containers: Map[String, Container] = Map.empty
  //有条件的读写控制，多个读者，单个写者，读不到等待
  private val rwLock = new ReentrantReadWriteLock()
  private val available = rwLock.writeLock().newCondition()

  def get(id: String): Container = {
    Locks.withReadLock(rwLock) {
      Container.containers.getOrElse(id, Default.get)
    }
  }

  def register(c: Container): Unit = {
    assert(null != c.id)
    Locks.withWriteLock(rwLock) {
      if (c.id == "ROOT") Default = Some(c)
      containers = containers + (c.id -> c)
      available.signalAll()
    }
  }

  def unregister(c: Container): Unit = {
    Locks.withWriteLock(rwLock) {
      containers -= c.id
      if (Default.contains(c)) Default = None
    }
  }

  def getOrAwait(id: String): Container = {
    Locks.withReadLock(rwLock) {
      Container.containers.get(id)
    } match {
      case Some(c) => c
      case None =>
        Locks.awaitCondition(rwLock.writeLock(), available)(Container.containers.contains(id)) {
          Container.containers(id)
        }
    }
  }
}

trait ContainerListener {
  def onStarted(container: Container): Unit = {}
}
