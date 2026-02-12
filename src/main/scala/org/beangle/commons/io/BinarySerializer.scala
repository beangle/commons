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

import org.beangle.commons.activation.{MediaType, MediaTypes}

import java.io.*

/** Binary serialize/deserialize (byte arrays).
 *
 * @author chaostone
 */
trait BinarySerializer extends Serializer, Deserializer {

  override def mediaTypes: Seq[MediaType] = List(MediaTypes.stream)

  /** Registers a class for serialization. */
  def registerClass(clazz: Class[_]): Unit

  /** Serializes object to byte array. */
  def asBytes(data: Any): Array[Byte]

  /** Deserializes byte array to object.
   *
   * @param clazz the target class
   * @param data  the byte array
   * @return the deserialized object
   */
  def asObject[T](clazz: Class[T], data: Array[Byte]): T
}

/** Base implementation with register-based ObjectSerializer mapping. */
abstract class AbstractBinarySerializer extends BinarySerializer {
  private var serializers = Map.empty[Class[_], ObjectSerializer]

  /** Registers an ObjectSerializer for the class. */
  def register(clazz: Class[_], os: ObjectSerializer): Unit =
    serializers += (clazz -> os)

  /** Serializes data to stream using the registered ObjectSerializer. */
  def serialize(data: Any, os: OutputStream, params: Map[String, Any]): Unit =
    if (null != data)
      serializers.get(data.getClass) match {
        case Some(serializer) => serializer.serialize(data, os, params)
        case None => throw new RuntimeException(s"Cannot find ${data.getClass.getName}'s corresponding ObjectSerializer.")
      }

  /** Deserializes from stream using the registered ObjectSerializer and closes stream. */
  def deserialize[T](clazz: Class[T], is: InputStream, params: Map[String, Any]): T =
    serializers.get(clazz) match {
      case Some(serializer) =>
        val rs = serializer.deserialize(is, params).asInstanceOf[T]
        IOs.close(is)
        rs
      case None => throw new RuntimeException(s"Cannot find ${clazz.getName}'s ObjectSerializer.")
    }

  override def asBytes(data: Any): Array[Byte] = {
    val os = new ByteArrayOutputStream
    serialize(data, os, Map.empty)
    os.toByteArray
  }

  override def asObject[T](clazz: Class[T], data: Array[Byte]): T =
    deserialize(clazz, new ByteArrayInputStream(data), Map.empty)
}

/** Default binary serializer for Externalizable/Serializable classes. */
object DefaultBinarySerializer extends AbstractBinarySerializer {

  /** Registers Externalizable or Serializable class with Java serialization. */
  def registerClass(clazz: Class[_]): Unit =
    if (classOf[Externalizable].isAssignableFrom(clazz) || classOf[java.io.Serializable].isAssignableFrom(clazz))
      register(clazz, ObjectSerializer.Default)
    else
      throw new RuntimeException("DefaultBinarySerializer only supports class implements Externalizable or Serializable")
}
