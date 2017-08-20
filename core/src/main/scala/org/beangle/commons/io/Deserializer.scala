package org.beangle.commons.io

import java.io.InputStream

trait Deserializer {

  def deserialize[T](clazz: Class[T], is: InputStream, params: Map[String, Any]): T
}