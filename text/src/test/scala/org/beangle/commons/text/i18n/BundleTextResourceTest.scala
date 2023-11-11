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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.Locale

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
      val tr = new DefaultTextResource(locale, registry, new DefaultTextFormatter())
      tr("hello.world") should equal(Some("你好"))
      tr("china") should equal(Some("中国"))
      tr("hello", "hello", "Jack") should equal("你好 Jack")
    }

    it("read Bundles") {
      val bundles = new DefaultTextBundleLoader().load(Locale.SIMPLIFIED_CHINESE, "org.beangle.commons.text.i18n.package")
      assert(null != bundles)
      assert(bundles.contains("org.beangle.commons.text.i18n.package"))
      val thisMap = bundles("org.beangle.commons.text.i18n.package")
      assert(thisMap.texts.size == 2)
      assert(bundles.contains("org.beangle.commons.text.i18n.Country"))
      val countryMap = bundles("org.beangle.commons.text.i18n.Country")
      assert(countryMap.texts.size == 1)
    }
    it("load class bundles") {
      val loader = new DefaultTextBundleLoader()
      val bundles = loader.load(Locale.SIMPLIFIED_CHINESE, "org.beangle.commons.text.i18n.Country")
      assert(null != bundles)
      assert(bundles.size == 2)
      assert(bundles.contains("org.beangle.commons.text.i18n.Country"))
      val thisMap = bundles("org.beangle.commons.text.i18n.Country")
      assert(thisMap.texts.contains("name"))

      assert(loader.load(Locale.SIMPLIFIED_CHINESE, "a.b").head._2.texts.isEmpty)
    }
  }
}
