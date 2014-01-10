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
package org.beangle.commons.text.i18n

import java.util.Locale
import org.beangle.commons.text.i18n.impl.DefaultTextBundleRegistry
import org.beangle.commons.text.i18n.impl.DefaultTextFormater
import org.beangle.commons.text.i18n.impl.DefaultTextResource
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class BundleTextResourceTest extends FunSpec with ShouldMatchers {

  describe("TextBundle") {
    it("Get text from bundle") {
      val locale = new Locale("zh", "CN")
      val registry = new DefaultTextBundleRegistry()
      val bundle = registry.load(locale, "message")
      val bundle2 = registry.load(locale, "message2")
      bundle.getText("hello.world") should equal(Some("你好"))
      val tr = new DefaultTextResource(locale, registry, new DefaultTextFormater())
      tr.getText("hello.world") should equal(Some("你好"))
      tr.getText("china") should equal(Some("中国"))
    }
  }
}
