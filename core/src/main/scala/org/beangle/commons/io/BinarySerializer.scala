/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.io

import org.beangle.commons.activation.MimeTypes
import javax.activation.MimeType
import java.io.OutputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ByteArrayInputStream
import java.io.ObjectOutputStream

/**
 * @author chaostone
 */
trait BinarySerializer extends Serializer {

  override def mediaTypes: Seq[MimeType] = {
    List(MimeTypes.ApplicationOctetStream)
  }

  override def serialize(data: Any, os: OutputStream, params: Map[String, Any]): Unit = {
    os.write(serialize(data, params))
  }

  def register(clazz: Class[_]): Unit = {

  }

  def serialize(data: Any, params: Map[String, Any]): Array[Byte]

  def deserialize(bits: Array[Byte], params: Map[String, Any]): AnyRef
}

object DefaultBinarySerializer extends BinarySerializer {

  def serialize(data: Any, params: Map[String, Any]): Array[Byte] = {
    val bos = new ByteArrayOutputStream
    val oos = new ObjectOutputStream(bos)
    oos.writeObject(data)
    bos.toByteArray
  }

  def deserialize(bits: Array[Byte], params: Map[String, Any]): AnyRef = {
    val bis = new ByteArrayInputStream(bits)
    val ois = new ObjectInputStream(bis)
    ois.readObject()
  }
}