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

package org.beangle.commons.lang.reflect

import java.lang.invoke.MethodHandle
import java.lang.invoke.{MethodHandles, VarHandle}
import java.lang.reflect.Method

/** VarHandle lookup helpers. The caller passes its own [[MethodHandles.Lookup]], which must have
 *  access to the target field (e.g. a lookup created inside the declaring class for private fields). */
object Invokers {

  /** Creates a VarHandle for the named instance field of the given class. */
  def findVarHandle(lookup: MethodHandles.Lookup, clazz: Class[_], name: String, fieldType: Class[_]): VarHandle =
    lookup.findVarHandle(clazz, name, fieldType)

  /** Creates a VarHandle for the named static field of the given class. */
  def findStaticVarHandle(lookup: MethodHandles.Lookup, clazz: Class[_], name: String, fieldType: Class[_]): VarHandle =
    lookup.findStaticVarHandle(clazz, name, fieldType)

  /** Unreflects a Method into a MethodHandle (setAccessible fallback for non-public classes). */
  def unreflect(lookup: MethodHandles.Lookup, m: Method): MethodHandle = {
    try lookup.unreflect(m)
    catch
      case _: IllegalAccessException =>
        m.setAccessible(true)
        lookup.unreflect(m)
  }
}
