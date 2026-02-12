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

package org.beangle.commons.io

import java.io.InputStream

/** Deserializes objects from an input stream. */
trait Deserializer {

  /** Deserializes an object from the input stream and closes it.
   *
   * @param clazz  the target class
   * @param is     the input stream
   * @param params optional deserialization parameters
   * @return the deserialized object
   */
  def deserialize[T](clazz: Class[T], is: InputStream, params: Map[String, Any]): T
}
