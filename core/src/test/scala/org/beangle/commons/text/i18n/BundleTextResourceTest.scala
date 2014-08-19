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
package org.beangle.commons.text.i18n

import java.util.Locale
import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BundleTextResourceTest extends FunSpec with Matchers {

  describe("TextBundle") {
    it("Get text from bundle") {
      val locale = new Locale("zh", "CN")
      val registry = new DefaultTextBundleRegistry()
      val bundle = registry.load(locale, "message")
      val bundle2 = registry.load(locale, "message2")
      bundle.get("hello.world") should equal(Some("你好"))
      val tr = new DefaultTextResource(locale, registry, new DefaultTextFormater())
      tr("hello.world") should equal(Some("你好"))
      tr("china") should equal(Some("中国"))
    }
  }
}
