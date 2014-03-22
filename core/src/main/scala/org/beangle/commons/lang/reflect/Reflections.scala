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
package org.beangle.commons.lang.reflect

import java.beans.BeanInfo
import java.beans.IntrospectionException
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Throwables
import org.beangle.commons.lang.Objects

object Reflections {

  def newInstance[T](clazz: Class[T]): T = {
    try {
      return clazz.newInstance()
    } catch {
      case e: Exception => Throwables.propagate(e)
    }
    Objects.default(clazz)
  }
}
