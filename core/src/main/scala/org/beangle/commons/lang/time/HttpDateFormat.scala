package org.beangle.commons.lang.time

import java.util.Date
import java.text.SimpleDateFormat
import java.util.TimeZone

/**
 *  Preferred HTTP date format (RFC 1123).
 *  @see https://www.ietf.org/rfc/rfc1123.txt
 */
object HttpDateFormat {

  val format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
  format.setTimeZone(TimeZone.getTimeZone("GMT"))

  def format(date: Date): String = {
    format.format(date)
  }
}