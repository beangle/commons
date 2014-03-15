/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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

import org.beangle.commons.http.agent.Engines._
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.beangle.commons.lang.Strings

object Browsers extends Enumeration {

  val Firefox = new Category("Firefox", Gecko, "Firefox/(\\S*)->$1", "Firefox")

  val Thunderbird = new Category("Thunderbird", Gecko, "Thunderbird/(\\S*)->$1", "Thunderbird")

  val Camino = new Category("Camino", Gecko, "Camino/(\\S*)->$1", "Camino")

  val Flock = new Category("Flock", Gecko, "Flock/(\\S*)->$1")

  val FirefoxMobile = new Category("Firefox Mobile", Gecko, "Firefox/3.5 Maemo->3")

  val SeaMonkey = new Category("SeaMonkey", Gecko, "SeaMonkey")

  val Tencent = new Category("Tencent Traveler", Trident, "TencentTraveler (\\S*);->$1")

  val Sogo = new Category("Sogo", Trident, "SE(.*)MetaSr")

  val TheWorld = new Category("The World", Trident, "theworld")

  val IE360 = new Category("Internet Explorer 360", Trident, "360SE")

  val IeMobile = new Category("IE Mobile", Trident, "IEMobile (\\S*)->$1")

  val IE = new Category("Internet Explorer", Trident, "MSIE (\\S*);->$1", "MSIE")

  val OutlookExpress = new Category("Windows Live Mail", Trident, "Outlook-Express/7.0->7.0")

  val Maxthon = new Category("Maxthon", WebKit, "Maxthon/(\\S*)->$1", "Maxthon")

  val Chrome = new Category("Chrome", WebKit, "Chrome/(\\S*)->$1", "Chrome")

  val Safari = new Category("Safari", WebKit, "Version/(\\S*) Safari->$1", "Safari")

  val Omniweb = new Category("Omniweb", WebKit, "OmniWeb")

  val AppleMail = new Category("Apple Mail", WebKit, "AppleWebKit")

  val ChromeMobile = new Category("Chrome Mobile", WebKit, "CrMo/(\\S*)->$1")

  val SafariMobile = new Category("Mobile Safari", WebKit, "Mobile Safari", "Mobile/5A347 Safari",
    "Mobile/3A101a Safari", "Mobile/7B367 Safari")

  val Silk = new Category("Silk", WebKit, "Silk/(\\S*)->$1")

  val Dolfin = new Category("Samsung Dolphin", WebKit, "Dolfin/(\\S*)->$1")

  val Opera = new Category("Opera", Presto, "Opera/(.*?)Version/(\\S*)->$2", "Opera Mini->Mini",
    "Opera")

  val Konqueror = new Category("Konqueror", Khtml, "Konqueror")

  val Outlook = new Category("Outlook", Word, "MSOffice 12->2007", "MSOffice 14->2010", "MSOffice")

  val LotusNotes = new Category("Lotus Notes", Other, "Lotus-Notes")

  val Bot = new Category("Robot/Spider", Other, "Googlebot", "bot", "spider", "crawler", "Feedfetcher",
    "Slurp", "Twiceler", "Nutch", "BecomeBot")

  val Mozilla = new Category("Mozilla", Other, "Mozilla", "Moozilla")

  val CFNetwork = new Category("CFNetwork", Other, "CFNetwork")

  val Eudora = new Category("Eudora", Other, "Eudora", "EUDORA")

  val PocoMail = new Category("PocoMail", Other, "PocoMail")

  val TheBat = new Category("The Bat!", Other, "The Bat")

  val NetFront = new Category("NetFront", Other, "NetFront")

  val Evolution = new Category("Evolution", Other, "CamelHttpStream")

  val Lynx = new Category("Lynx", Other, "Lynx")

  val UC = new Category("UC", Other, "UCWEB")

  val Download = new Category("Downloading Tool", Other, "cURL", "wget")

  val Unknown = new Category("Unknown", Other)

  class Category(val name: String, val engine: Engine, versions: String*) extends Val {
    private val versionPairs = build(versions)

    engine.addCategory(this)

    private def build(versions: Seq[String]): List[Pair[Pattern, String]] = {
      val pairs = new collection.mutable.ListBuffer[Pair[Pattern, String]]
      for (version <- versions) {
        var matcheTarget = version
        var versionNum = ""
        if (Strings.contains(version, "->")) {
          matcheTarget = "(?i)" + Strings.substringBefore(version, "->")
          versionNum = Strings.substringAfter(version, "->")
        }
        pairs += Pair(Pattern.compile(matcheTarget), versionNum)
      }
      pairs.toList
    }

    def matches(agentString: String): String = {
      for (pair <- versionPairs) {
        val m = pair._1.matcher(agentString)
        if (m.find()) {
          val sb = new StringBuffer()
          m.appendReplacement(sb, pair._2)
          sb.delete(0, m.start())
          return sb.toString
        }
      }
      null
    }
  }

  import scala.language.implicitConversions
  implicit def convertValue(v: Value): Category = v.asInstanceOf[Category]
}
