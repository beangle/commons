/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
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