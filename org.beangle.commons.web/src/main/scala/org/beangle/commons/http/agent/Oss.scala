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

import java.util.regex.Matcher
import java.util.regex.Pattern
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.tuple.Pair
import scala.collection.mutable

object Oss extends Enumeration {

  val Windows = new Category("Windows", "Windows NT 6.1->7", "Windows NT 6->Vista", "Windows NT 5.0->2000", 
    "Windows NT 5->XP", "Win98->98", "Windows 98->98", "Windows Phone OS 7->Mobile 7", "Windows CE->Mobile", 
    "Windows")

  val Android = new Category("Android", "Android-4->4.x", "Android 4->4.x", "Xoom->4.x Tablet", "Transformer->4.x Tablet", 
    "Android 3->3.x Tablet", "Android 2->2.x", "Kindle Fire->2.x Tablet", "GT-P1000->2.x Tablet", "SCH-I800->2.x Tablet", 
    "Android 1->1.x", "GoogleTV->(Google TV)", "Android")

  val Linux = new Category("Linux", "Fedora/(\\S*)\\.fc(\\S*)->Fedora fc$2", "Ubuntu/(\\S*)->Ubuntu $1", 
    "Fedora", "Ubuntu", "Linux", "CamelHttpStream")

  val Webos = new Category("WebOS", "webOS")

  val Palm = new Category("PalmOS", "Palm")

  val Ios = new Category("iOS", "iPhone OS(\\S*)->$1 (iPhone)", "like Mac OS X", "iOS")

  val MacOs = new Category("Mac OS", "iPad->(iPad)", "iPhone->(iPhone)", "iPod->(iPod)", "Mac OS X->X", 
    "CFNetwork->X", "Mac")

  val Maemo = new Category("Maemo", "Maemo")

  val Bada = new Category("Bada", "Bada")

  val Kindle = new Category("Kindle", "Kindle/(\\S*)->(Kindle $1)", "Kindle")

  val Symbian = new Category("Symbian OS", "SymbianOS/(\\S*)->$1", "Series60/3->9.x", "Series60/2.6->8.x", 
    "Series60/2.8->8.x", "Symbian", "Series60")

  val Series40 = new Category("Series 40", "Nokia6300")

  val SonyEricsson = new Category("Sony Ericsson", "SonyEricsson")

  val SunOs = new Category("SunOS", "SunOS")

  val Psp = new Category("Sony Playstation", "Playstation")

  val Wii = new Category("Nintendo Wii", "Wii")

  val BlackBerry = new Category("BlackBerryOS", "(BB|BlackBerry|PlayBook)(.*?)Version/(\\S*)->$3", 
    "BlackBerry")

  val Roku = new Category("Roku OS", "Roku")

  val Unknown = new Category("Unknown")

  class Category(val name: String, versions: String*) extends Val {

    private val versionPairs = new mutable.ListBuffer[Pair[Pattern,String]]

    for (version <- versions) {
      var matcheTarget = version
      var versionNum = ""
      if (Strings.contains(version, "->")) {
        matcheTarget = "(?i)" + Strings.substringBefore(version, "->")
        versionNum = Strings.substringAfter(version, "->")
      }
      versionPairs+=Pair.of(Pattern.compile(matcheTarget), versionNum)
    }

    def matches(agentString: String): String = {
      for (entry <- versionPairs) {
        val m = entry._1.matcher(agentString)
        if (m.find()) {
          val sb = new StringBuffer()
          m.appendReplacement(sb, entry._2)
          sb.delete(0, m.start())
          return sb.toString
        }
      }
      null
    }
  }

  implicit def convertValue(v: Value): Category = v.asInstanceOf[Category]
}
