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

import org.beangle.commons.cdi.spring.bean.{ AdvancedUserLdapProvider, UserDaoProvider, UserLdapProvider, UserService }
import org.scalatest.{ FunSpec, Matchers }
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.cdi.spring.bean.NoneDao
import org.beangle.commons.cdi.spring.bean.ProviderManager

@RunWith(classOf[JUnitRunner])
class ReconfigProcessorTest extends FunSpec with Matchers {

  describe("ReconfigProcessor") {
    it("testGetDefinition") {
      val factory = new ClassPathXmlApplicationContext("/org/beangle/cdi/spring/context.xml")
      // two user provider
      factory.getBean("userDaoProvider") should not be (null)

      factory.getBean("userLdapProvider") should not be (null)

      // userService
      val userService = factory.getBean("userService").asInstanceOf[UserService]

      userService should not be (null)

      userService.someMap should not be (null)

      userService.provider.getClass() should equal(classOf[UserDaoProvider])

      // userLdapService
      val userLdapService = factory.getBean("userLdapService").asInstanceOf[UserService]

      userLdapService should not be (null)

      userLdapService.provider.getClass() should equal(classOf[UserLdapProvider])
    }

    it("Override") {
      val factory = new ClassPathXmlApplicationContext("/org/beangle/cdi/spring/context-config.xml")
      // userService
      val userService = factory.getBean("userService").asInstanceOf[UserService]

      userService should not be (null)

      // unmerged map
      userService.someMap should not be (null)

      userService.someMap.size should be(1)

      userService.someMap("string") should be("override string")

      // merged list
      userService.someList.size should be(3)

      // change class
      val ldapProvider = factory.getBean("userLdapProvider").asInstanceOf[UserLdapProvider]
      ldapProvider.isInstanceOf[AdvancedUserLdapProvider] should be(true)

      val userLdapService = factory.getBean("userLdapService").asInstanceOf[UserService]
      userLdapService should not be (null)

      userLdapService.provider.getClass() should equal(classOf[AdvancedUserLdapProvider])
    }

    it("Get Singleton object") {
      val factory = new ClassPathXmlApplicationContext("/org/beangle/cdi/spring/context-config.xml")
      factory.getBean("noneDao") should be(NoneDao)
    }

    it("Auto wire map") {
      val factory = new ClassPathXmlApplicationContext("/org/beangle/cdi/spring/context-config.xml")
      val managers = factory.getBeansOfType(classOf[ProviderManager])
      assert(!managers.isEmpty())
      assert(managers.values.iterator().next.providerMap.size == 2)
    }
  }
}
