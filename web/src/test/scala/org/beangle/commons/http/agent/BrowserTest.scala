/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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

@RunWith(classOf[JUnitRunner])
class BrowserTest extends FunSpec with Matchers {

  val firefox3 = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.14) Gecko/2009090216 Ubuntu/9.04 (jaunty) Firefox/3.0.14"

  val firefox4 = Array("Mozilla/5.0 (X11; Linux x86_64; rv:2.0b4) Gecko/20100818 Firefox/4.0b4", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:2.0b9pre) Gecko/20101228 Firefox/4.0b9pre", "Firefox")

  val ie = Array("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; T312461)", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Zune 4.0; InfoPath.3; MS-RTC LM 8; .NET4.0C; .NET4.0E)")

  val chrome = Array("Mozilla/5.0 (Windows; U; Windows NT 5.2; en-US) AppleWebKit/532.9 (KHTML, like Gecko) Chrome/5.0.310.0 Safari/532.9", "Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/532.9 (KHTML, like Gecko) Chrome/5.0.309.0 Safari/532.9")

  val opera = Array("Opera/9.80 (X11; Linux x86_64; U; bg) Presto/2.8.131 Version/11.10", "Opera/9.80 (Windows NT 5.2; U; zh-cn) Presto/2.6.30 Version/10.63", "Opera/12.80 (Windows NT 5.1; U; en) Presto/2.10.289 Version/12.02")

  val sogo = Array("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/4.0; EasyBits GO v1.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; SE 2.X MetaSr 1.0)")

  val maxthon = Array("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.1 (KHTML, like Gecko) Maxthon/4.0.3.1000 Chrome/22.0.1229.79 Safari/537.1")

  val theworld = Array("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; EasyBits GO v1.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; qihu theworld)")

  val validator = Array("Total Validator")

  describe("Browser") {
    it("Parser usage string") {
      var browser = Browser.parse(firefox3)
      browser.version should equal("3.0.14")
      browser.category should equal(Browsers.Firefox)
      browser = Browser.parse(firefox4(0))
      browser.version should equal("4.0b4")
      browser = Browser.parse(firefox4(1))
      browser.version should equal("4.0b9pre")
      browser = Browser.parse(firefox4(2))
      browser.version should equal("")
      browser.category should equal(Browsers.Firefox)
      browser = Browser.parse(ie(0))
      browser.version should equal("6.0")
      browser.category should equal(Browsers.IE)
      browser = Browser.parse(chrome(0))
      browser.version should equal("5.0.310.0")
      browser.category should equal(Browsers.Chrome)
      Browser.parse(opera(0)).version should equal("11.10")
      Browser.parse(opera(1)).version should equal("10.63")
      Browser.parse(opera(2)).version should equal("12.02")
      browser = Browser.parse(validator(0))
      browser.version should be(null)
      browser.category should equal(Browsers.Unknown)
      browser = Browser.parse(maxthon(0))
      browser.category should equal(Browsers.Maxthon)
      browser = Browser.parse(sogo(0))
      browser.category should equal(Browsers.Sogo)
      browser = Browser.parse(theworld(0))
      browser.category should equal(Browsers.TheWorld)
    }
  }
}
