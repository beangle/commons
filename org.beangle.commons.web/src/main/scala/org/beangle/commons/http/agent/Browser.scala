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
import Browser._
//remove if not needed
import scala.collection.JavaConversions._

object Browser {

  var browsers: Map[String, Browser] = CollectUtils.newHashMap()

  val UNKNOWN = new Browser(Browsers.Unknown, null)

  /**
   * Iterates over all Browsers to compare the browser signature with the user
   * agent string. If no match can be found Browser.UNKNOWN will be returned.
   *
   * @param agentString
   * @return Browser
   */
  def parse(agentString: String): Browser = {
    if (Strings.isEmpty(agentString)) {
      return Browser.UNKNOWN
    }
    for (engine <- Engines.values) {
      val egineName = engine.name
      if (agentString.contains(egineName)) {
        for (category <- engine.browserCategories) {
          val version = category.`match`(agentString)
          if (version != null) {
            val key = category.getName + "/" + version
            var browser = browsers.get(key)
            if (null == browser) {
              browser = new Browser(category, version)
              browsers.put(key, browser)
            }
            return browser
          }
        }
      }
    }
    for (category <- Browsers.values) {
      val version = category.`match`(agentString)
      if (version != null) {
        val key = category.getName + "/" + version
        var browser = browsers.get(key)
        if (null == browser) {
          browser = new Browser(category, version)
          browsers.put(key, browser)
        }
        return browser
      }
    }
    Browser.UNKNOWN
  }
}

/**
 * Web browser
 *
 * @author chaostone
 */
@SerialVersionUID(-6200607575108416928L)
class Browser(val category: Browsers.Category, val version: String) extends Serializable() with Comparable[Browser] {

  override def toString(): String = {
    category.getName + " " + (if (version == null) "" else version)
  }

  def compareTo(o: Browser): Int = category.compareTo(o.category)
}
