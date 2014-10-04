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
package org.beangle.commons.config.property

import org.beangle.commons.inject.Resources
import org.beangle.commons.lang.ClassLoaders
import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UrlConfigProviderTest extends FunSpec with Matchers {

  describe("UrlConfigProvider") {
    it("Get Property") {
      val config = new MultiProviderPropertyConfig
      val provider = new UrlPropertyConfigProvider
      val resources = new Resources(ClassLoaders.getResource("system-default.properties"), null, ClassLoaders.getResource("system.properties"))
      provider.resources = resources
      val properties = provider.getConfig
      config.add(properties)
      config.get(classOf[Integer], "testInt") should be(Some(1))
      config.get("system.vendor") should equal(Some("beangle.org"))
      config.get("system.url") should equal(Some("http://localhost"))
    }
  }
}
