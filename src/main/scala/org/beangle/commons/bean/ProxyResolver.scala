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

package org.beangle.commons.bean

/** ProxyResolver factory. */
object ProxyResolver {

  /** No-op resolver; returns object as-is. */
  object Null extends ProxyResolver {

    def isProxy(obj: AnyRef): Boolean = false

    def targetClass(obj: AnyRef): Class[_] = obj.getClass

    def unproxy(obj: AnyRef): AnyRef = obj
  }
}

/** Resolves proxy to target class/instance. */
trait ProxyResolver {

  /** Returns true if obj is a proxy. */
  def isProxy(obj: AnyRef): Boolean

  /** Returns the target (unproxied) class. */
  def targetClass(obj: AnyRef): Class[_]

  /** Returns the unproxied instance. */
  def unproxy(obj: AnyRef): AnyRef
}
