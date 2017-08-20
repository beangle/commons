package org.beangle.commons.io

import java.io.{ InputStream, ObjectInputStream, ObjectOutputStream, OutputStream }

trait ObjectSerializer {

  def serialize(data: Any, os: OutputStream, params: Map[String, Any]): Unit

  def deserialize(is: InputStream, params: Map[String, Any]): Any
}

object ObjectSerializer {

  object Default extends ObjectSerializer {
    def serialize(data: Any, os: OutputStream, params: Map[String, Any]): Unit = {
      val oos = new ObjectOutputStream(os)
      oos.writeObject(data)
      oos.flush()
    }

    def deserialize(is: InputStream, params: Map[String, Any]): Any = {
      new ObjectInputStream(is).readObject()
    }
  }
}