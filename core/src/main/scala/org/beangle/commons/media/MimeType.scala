package org.beangle.commons.media

import org.beangle.commons.lang.Strings

object MimeType {

  val All = this("*/*")

  val ApplicationAtomXml = this("application/atom+xml")

  val ApplicationFormUrlencoded = this("application/x-www-form-urlencoded");

  val ApplicationJson = this("application/json")

  val ApplicationOctetStream = this("application/octet-stream");

  val ApplicationXhtmlXml = this("application/xhtml+xml")

  val ApplicationXml = this("application/xml")

  val ImageGif = this("image/gif");

  val ImageJpeg = this("image/jpeg")

  val ImagePng = this("image/png")

  val MultipartFormData = this("multipart/form-data")

  val TextHtml = this("text/html")

  val TextPlain = this("text/plain")

  val TextXml = this("text/xml")

  val PARAM_QUALITY_FACTOR = "q";

  def apply(mimeType: String): MimeType = {
    val wildCard = "*"
    val parts = Strings.split(mimeType, ";")

    var fullType = parts(0).trim
    if (wildCard.equals(fullType)) fullType = "*/*"

    val subIndex = fullType.indexOf('/')
    require(subIndex > -1, mimeType + "does not contain '/'")
    require(subIndex != fullType.length() - 1, mimeType + "does not contain subtype after '/'")

    val maintype = fullType.substring(0, subIndex)
    val subtype = fullType.substring(subIndex + 1, fullType.length)

    require(maintype != wildCard || subtype == wildCard, mimeType + "wildcard type is legal only in '*/*' (all mime types)")

    val parameters = new collection.mutable.HashMap[String, String]
    if (parts.length > 1) {
      for (i <- 1 until parts.length) {
        val parameter = parts(i)
        val eqIndex = parameter.indexOf('=')
        if (eqIndex != -1)
          parameters.put(parameter.substring(0, eqIndex), parameter.substring(eqIndex + 1, parameter.length))
      }
    }
    new MimeType(maintype, subtype, parameters.toMap)
  }

}

class MimeType(val maintype: String, val subtype: String, val parameters: Map[String, String]) {

  override def toString: String = {
    val builder = new StringBuilder()
    builder.append(maintype)
    builder.append('/')
    builder.append(this.subtype)
    parameters foreach {
      case (k, v) =>
        builder.append(';')
        builder.append(k)
        builder.append('=')
        builder.append(v)
    }
    builder.toString
  }

  override def equals(other: Any): Boolean = {
    if (this eq other.asInstanceOf[Object]) return true
    other match {
      case ot: MimeType => (this.maintype.equalsIgnoreCase(ot.maintype) &&
        this.subtype.equalsIgnoreCase(ot.subtype) &&
        this.parameters.equals(ot.parameters));
      case _ => false
    }
  }

  override def hashCode(): Int = {
    var result = this.maintype.hashCode();
    result = 31 * result + this.subtype.hashCode();
    result = 31 * result + this.parameters.hashCode();
    return result
  }

}