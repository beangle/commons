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

package org.beangle.commons.lang

import org.beangle.commons.lang.annotation.beta

import java.util.concurrent.atomic.AtomicReference

@beta
object ScopedContext {
  private val context = ScopedValue.newInstance[Holder]()

  case class Key[T](name: String)

  private class Holder(var values: Map[String, Any]) {

    def add(k: String, v: Any): Unit = {
      values = values + (k -> v)
    }

    def remove(k: String): Unit = {
      values = values - k
    }

    def get(k: String): Option[Any] = {
      values.get(k)
    }
  }

  def get[T](key: Key[T]): Option[T] = {
    if (context.isBound) {
      context.get.get(key.name).map(_.asInstanceOf[T])
    } else {
      None
    }
  }

  def put[T](key: Key[T], value: T): Unit = {
    if (context.isBound) {
      throw new IllegalAccessException("ScopedContext was not bounded")
    }
    context.get.add(key.name, value)
  }

  /** Execute body with the given data. */
  def runWith[A](data: (Key[_], Any)*)(body: => A): A = {
    val holder = new Holder(data.map(x => (x._1.name, x._2)).toMap)
    val result = AtomicReference[A]
    //dont using call,for it was changed in JDK25
    ScopedValue.where(context, holder).run { () =>
      result.set(body)
    }
    result.get
  }
}
