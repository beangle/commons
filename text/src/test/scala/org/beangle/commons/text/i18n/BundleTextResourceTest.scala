/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.text.i18n

import java.util.Locale

import org.beangle.commons.lang.ClassLoaders

import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec

class BundleTextResourceTest extends AnyFunSpec with Matchers {

  describe("TextBundle") {
    it("read message by class") {
      val messages = Messages(Locale.SIMPLIFIED_CHINESE)
      assert(messages.get(classOf[Country], "name") == "名称")
      assert(messages.get(classOf[City], "name") == "name")
    }

    it("Get text from bundle") {
      val locale = new Locale("zh", "CN")
      val registry = new DefaultTextBundleRegistry()
      val bundle = registry.load(locale, "message")
      val bundle3 = registry.load(locale, "org.beangle.commons.text.i18n.Country")
      val bundle2 = registry.load(locale, "org.beangle.commons.text.i18n.package")
      assert(null != bundle3)
      bundle3.get("name") should equal(Some("名称"))
      bundle.get("hello.world") should equal(Some("你好"))
      val tr = new DefaultTextResource(locale, registry, new DefaultTextFormater())
      tr("hello.world") should equal(Some("你好"))
      tr("china") should equal(Some("中国"))
      tr("hello", "hello", "Jack") should equal("你好 Jack")
    }

    it("read Bundles") {
      val url = ClassLoaders.getResource("org/beangle/commons/text/i18n/package.zh_CN").get
      val bundles = new DefaultTextBundleRegistry().readBundles(url.openStream)
      assert(null != bundles)
      assert(bundles.contains(""))
      val thisMap = bundles("")
      assert(thisMap.size == 2)
      assert(bundles.contains("Country"))
      val countryMap = bundles("Country")
      assert(countryMap.size == 1)
    }
  }
}
