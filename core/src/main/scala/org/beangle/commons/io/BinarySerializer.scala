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

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, Externalizable, InputStream, OutputStream }

import org.beangle.commons.activation.MimeTypes

import javax.activation.MimeType

/**
 * @author chaostone
 */
trait BinarySerializer extends Serializer with Deserializer {

  override def mediaTypes: Seq[MimeType] = {
    List(MimeTypes.ApplicationOctetStream)
  }

  def registerClass(clazz: Class[_]): Unit

  def asBytes(data: Any): Array[Byte]

  def asObject[T](clazz: Class[T], data: Array[Byte]): T
}

abstract class AbstractBinarySerializer extends BinarySerializer {
  private var serializers = Map.empty[Class[_], ObjectSerializer]

  def register(clazz: Class[_], os: ObjectSerializer): Unit = {
    serializers += (clazz -> os)
  }

  def serialize(data: Any, os: OutputStream, params: Map[String, Any]): Unit = {
    if (null == data) return ;
    serializers.get(data.getClass) match {
      case Some(serializer) => serializer.serialize(data, os, params)
      case None             => throw new RuntimeException(s"Cannot find ${data.getClass.getName}'s corresponding ObjectSerializer.")
    }
  }

  def deserialize[T](clazz: Class[T], is: InputStream, params: Map[String, Any]): T = {
    serializers.get(clazz) match {
      case Some(serializer) =>
        val rs = serializer.deserialize(is, params).asInstanceOf[T]
        IOs.close(is)
        rs
      case None => throw new RuntimeException(s"Cannot find ${clazz.getName}'s ObjectSerializer.")
    }
  }

  override def asBytes(data: Any): Array[Byte] = {
    val os = new ByteArrayOutputStream
    serialize(data, os, Map.empty)
    os.toByteArray
  }

  override def asObject[T](clazz: Class[T], data: Array[Byte]): T = {
    deserialize(clazz, new ByteArrayInputStream(data), Map.empty)
  }

}

object DefaultBinarySerializer extends AbstractBinarySerializer {

  def registerClass(clazz: Class[_]): Unit = {
    if (classOf[Externalizable].isAssignableFrom(clazz) || classOf[java.io.Serializable].isAssignableFrom(clazz)) {
      register(clazz, ObjectSerializer.Default)
    } else {
      throw new RuntimeException("DefaultBinarySerializer only supports class implements Externalizable or Serializable")
    }
  }

}

