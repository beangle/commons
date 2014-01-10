/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.message.mail
import java.util.List
import javax.mail.internet.InternetAddress
import java.util.Collections
import org.beangle.commons.lang.Strings
import scala.collection.JavaConversions._

object MimeUtils {

  def parseAddress(address: String, encoding: String): List[InternetAddress] = {
    if (Strings.isEmpty(address)) Collections.emptyList()
    try {
      var parsed = InternetAddress.parse(address)
      var returned = new java.util.ArrayList[InternetAddress]()
      parsed.foreach(raw => returned.add(if (encoding != null) new InternetAddress(raw.getAddress(), raw.getPersonal(), encoding) else raw))
      returned
    } catch {
      case ex: Exception => throw new RuntimeException("Failed to parse embedded personal name to correct encoding", ex)
    }
  }
}
