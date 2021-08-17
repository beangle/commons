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

import org.beangle.commons.lang.reflect.Reflections

/**
  * Create Enumeration value
  *
  * @since 3.1
  */
object Enums {

  /**
    * Returns an optional enum constant for the given type, using {@link Enum# valueOf}. If the
    * constant does not exist, {@link Option# none} is returned. A common use case is for parsing
    * user input or falling back to a default enum constant. For example,
    * {@code Enums.get(Country.class, countryInput).getOrElse(Country.DEFAULT);}
    *
    * @since 3.1
    */
  def get[T <: _root_.scala.reflect.Enum](enumClass: Class[T], value: String): Option[T] = {
    try {
      println(enumClass)
      val e = Reflections.getInstance[AnyRef](enumClass.getName)
      Some(enumClass.getMethod("valueOf", classOf[String]).invoke(e, value).asInstanceOf[T])
    } catch {
      case iae: IllegalArgumentException => None
    }
  }

  def isEnum(clazz: Class[_]): Boolean = {
    classOf[_root_.scala.reflect.Enum].isAssignableFrom(clazz)
  }
}
