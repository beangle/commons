package org.beangle.commons.io

import java.io.OutputStream
import javax.activation.MimeType

trait Serializer {

  def serialize(data: AnyRef, os: OutputStream, params: Map[String, Any])

  def supportMediaTypes: Seq[MimeType]

}