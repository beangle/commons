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

import java.io.{InputStream, ObjectInputStream, ObjectOutputStream, OutputStream}

/** Serializes/deserializes a single object type. */
trait ObjectSerializer {

  /** Serializes data to the output stream. */
  def serialize(data: Any, os: OutputStream, params: Map[String, Any]): Unit

  /** Deserializes object from the input stream. */
  def deserialize(is: InputStream, params: Map[String, Any]): Any
}

/** ObjectSerializer implementations. */
object ObjectSerializer {

  /** Java ObjectOutputStream/ObjectInputStream-based serializer. */
  object Default extends ObjectSerializer {
    def serialize(data: Any, os: OutputStream, params: Map[String, Any]): Unit = {
      val oos = new ObjectOutputStream(os)
      oos.writeObject(data)
      oos.flush()
    }

    def deserialize(is: InputStream, params: Map[String, Any]): Any =
      new ObjectInputStream(is).readObject()
  }
}
