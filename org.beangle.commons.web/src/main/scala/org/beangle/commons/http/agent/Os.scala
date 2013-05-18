/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.http.agent

import java.io.Serializable
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import Os._
//remove if not needed
import scala.collection.JavaConversions._

object Os {

  var osMap: Map[String, Os] = CollectUtils.newHashMap()

  val UNKNOWN = new Os(Oss.Unknown, null)

  /**
   * Parses user agent string and returns the best match. Returns Os.UNKNOWN
   * if there is no match.
   *
   * @param agentString
   * @return Os
   */
  def parse(agentString: String): Os = {
    if (Strings.isEmpty(agentString)) {
      return Os.UNKNOWN
    }
    for (category <- Oss.values) {
      val version = category.`match`(agentString)
      if (version != null) {
        val key = category.getName + "/" + version
        var os = osMap.get(key)
        if (null == os) {
          os = new Os(category, version)
          osMap.put(key, os)
        }
        return os
      }
    }
    Os.UNKNOWN
  }
}

@SerialVersionUID(-7506270303767154240L)
class Os private (val category: Oss.Category, val version: String) extends Serializable() with Comparable[Os] {

  override def toString(): String = {
    category.getName + " " + (if (version == null) "" else version)
  }

  def compareTo(o: Os): Int = category.compareTo(o.category)
}
