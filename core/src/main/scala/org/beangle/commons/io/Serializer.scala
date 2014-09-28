package org.beangle.commons.io

import java.io.OutputStream

trait Serializer {

  def serialize(data: AnyRef, os: OutputStream)
}