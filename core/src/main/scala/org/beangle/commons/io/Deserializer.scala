package org.beangle.commons.io

import java.io.InputStream

trait Deserializer {

  /**
   * Deserializer a object from inputstream,then close then stream,return the object.
   */
  def deserialize[T](clazz: Class[T], is: InputStream, params: Map[String, Any]): T
}