/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.config.property

import org.beangle.commons.config.Resources
import org.beangle.commons.lang.ClassLoaders
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UrlConfigProviderTest extends AnyFunSpec with Matchers {

  describe("UrlConfigProvider") {
    it("Get Property") {
      val config = new MultiProviderPropertyConfig
      val provider = new UrlPropertyConfigProvider
      val resources = new Resources(ClassLoaders.getResource("system-default.properties"), List.empty, ClassLoaders.getResource("system.properties"))
      provider.resources = resources
      val properties = provider.getConfig
      config.add(properties)
      config.get(classOf[Integer], "testInt") should be(Some(1))
      config.get("system.vendor") should equal(Some("beangle.org"))
      config.get("system.url") should equal(Some("http://localhost"))
    }
  }
}
