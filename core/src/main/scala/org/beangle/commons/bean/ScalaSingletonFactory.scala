package org.beangle.commons.bean

class ScalaSingletonFactory[T](override val objectType: Class[T]) extends Factory[T] {

  val result = objectType.getField("MODULE$").get(null).asInstanceOf[T]

  override def singleton: Boolean = true
}