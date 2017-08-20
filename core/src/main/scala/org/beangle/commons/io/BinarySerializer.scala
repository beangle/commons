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

import java.io.{ Externalizable, InputStream, OutputStream }

import org.beangle.commons.activation.MimeTypes

import javax.activation.MimeType

/**
 * @author chaostone
 */
trait BinarySerializer extends Serializer with Deserializer {

  override def mediaTypes: Seq[MimeType] = {
    List(MimeTypes.ApplicationOctetStream)
  }

  def register(clazz: Class[_], os: ObjectSerializer): Unit

  def registerClass(clazz: Class[_]): Unit
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
      case None             => throw new RuntimeException("Cannot find coresponding ObjectSerializer,register it first.")
    }
  }

  def deserialize[T](clazz: Class[T], is: InputStream, params: Map[String, Any]): T = {
    serializers.get(clazz) match {
      case Some(serializer) => serializer.deserialize(is, params).asInstanceOf[T]
      case None             => throw new RuntimeException("Cannot find coresponding ObjectSerializer,register it first.")
    }
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

