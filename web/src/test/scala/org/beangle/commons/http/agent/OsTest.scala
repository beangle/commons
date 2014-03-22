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

import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * OS parse test
 */
@RunWith(classOf[JUnitRunner])
class OsTest extends FunSpec with Matchers {

  val windows = Array("Mozilla/4.0 (compatible; MSIE 6.0; Windows 98; Rogers HiáSpeed Internet; (R1 1.3))", "Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.8b3) Gecko/20050713 SeaMonkey/1.0a", "Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8 ( .NET CLR 3.5.30729)", "Mozilla/5.0 (compatible; MSIE 7.0; Windows NT 5.2; WOW64; .NET CLR 2.0.50727)", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506)")

  val linux = Array("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.12) Gecko/20101027 Fedora/3.6.12-1.fc14 Firefox/3.6.12")

  val blackberry = Array("Mozilla/5.0 (BB10; <Device Model>) AppleWebKit/537.10+ (KHTML, like Gecko) Version/10.0.9.1675 Mobile Safari/537.10+", "Mozilla/5.0 (PlayBook; U; RIM Tablet OS 2.0.0; en-US) AppleWebKit/535.8+ (KHTML, like Gecko) Version/7.2.0.0 Safari/535.8+", "Mozilla/5.0 (BlackBerry; U; BlackBerry AAAA; en-US) AppleWebKit/534.11+ (KHTML, like Gecko) Version/6.0.0.141 Mobile Safari/534.11+")

  describe("OS") {
    it("parse") {
      var os = Os.parse(windows(0))
      os.category should equal(Oss.Windows)
      os.version should equal("98")
      os = Os.parse(windows(2))
      os.version should equal("XP")
      os = Os.parse(windows(4))
      os.version should equal("Vista")
      os = Os.parse(blackberry(0))
      os.version should equal("10.0.9.1675")
      os = Os.parse(blackberry(1))
      os.version should equal("7.2.0.0")
      os = Os.parse(blackberry(2))
      os.version should equal("6.0.0.141")
      os = Os.parse(linux(0))
      os.version should equal("Fedora fc14")
    }
  }
}
