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
package org.beangle.commons.cdi.spring.config

import org.beangle.commons.cdi.bind.BindRegistry
import org.beangle.commons.cdi.spring.bean.{ProxyFactoryBean, TestDao, TestEntityDao}
import org.scalatest.{FunSpec, Matchers}
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader
import org.springframework.core.io.ClassPathResource
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DefinitionBindRegistryTest extends FunSpec with Matchers {

  describe("Spring XmlBeanDefinitionReader") {
    it("parse xml") {
      val factory = new DefaultListableBeanFactory()
      val reader = new XmlBeanDefinitionReader(factory)
      reader.loadBeanDefinitions(new ClassPathResource("/org/beangle/cdi/spring/context-registry.xml"))

      val registry = new SpringBindRegistry(factory)
      val names = registry.getBeanNames(classOf[TestDao])

      names should not be (null)
      names.size should be(1)

      names.contains("entityDao") should be(true)

      registry.contains("entityDao") should be(true)

      registry.getBeanType("entityDao") should equal(classOf[TestDao])

      val name2s = registry.getBeanNames(classOf[ProxyFactoryBean])
      name2s should not be (null)
      name2s.size should be(1)
      name2s.contains("&entityDao") should be(true)
    }
  }
}
