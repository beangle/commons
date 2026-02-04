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

import org.beangle.commons.lang.reflect.Reflections

object Factory {
  def getObjectType(clazz: Class[_]): Class[_] = {
    val objectTypes = Reflections.getGenericParamTypes(clazz, classOf[Factory[_]]).values
    if (objectTypes.isEmpty) throw new RuntimeException(s"Cannot find factory object type of class ${clazz.getName}")
    objectTypes.head
  }
}

trait Factory[T] {

  def getObject: T

  def singleton: Boolean = true

  def objectType: Class[T] = {
    Reflections.getGenericParamTypes(this.getClass, classOf[Factory[_]]).values.head.asInstanceOf[Class[T]]
  }
}
