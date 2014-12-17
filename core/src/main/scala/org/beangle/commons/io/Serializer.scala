package org.beangle.commons.io

import java.io.OutputStream
import javax.activation.MimeType

trait Serializer {

  def serialize(data: AnyRef, os: OutputStream, properties: Tuple2[Class[_], List[String]]*)

  def supportMediaTypes: Seq[MimeType]

}