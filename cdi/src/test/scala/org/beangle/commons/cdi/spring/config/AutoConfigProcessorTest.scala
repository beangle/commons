/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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

import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.beangle.commons.cdi.spring.bean.{ ResourcesConsumer, SomeAction, TestService, UserDaoProvider, UserLdapProvider }
import org.scalatest.{ FunSpec, Matchers }
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.cdi.Container

/**
 * Test Bean definition in Java config
 *
 * @author chaostone
 */
@RunWith(classOf[JUnitRunner])
class AutoConfigProcessorTest extends FunSpec with Matchers with Logging {

  describe("AutoConfigProcessor") {
    it("Test get normal and factory bean") {
      val watch = new Stopwatch(true)
      val factory = new ClassPathXmlApplicationContext("/org/beangle/cdi/spring/context-auto.xml")
      factory.getBean(classOf[Container]) should not be (null)
      testBean(factory)
      testFactoryBean(factory)
      logger.debug(s"config  context-auto completed using $watch")
    }

    it("testAdvance") {
      val watch = new Stopwatch(true)
      val factory = new ClassPathXmlApplicationContext("/org/beangle/cdi/spring/context-auto.xml")
      // test Alias
      factory.getBean(classOf[TestService].getName()) should not be (null)
      val consumer = factory.getBean(classOf[ResourcesConsumer].getName()).asInstanceOf[ResourcesConsumer]
      consumer should not be (null)
      consumer.resources should not be (null)
      logger.debug(s"config  advance context-auto completed using $watch")

    }

  }

  private def testFactoryBean(factory: ApplicationContext) {
    val testService = factory.getBean("testService", classOf[TestService])
    testService should not be (null)
    testService.entityDao should not be (null)
    testService.noneDao should be(null)
  }

  private def testBean(factory: ApplicationContext) {
    // two user provider
    val daoProvider = factory.getBean("userDaoProvider").asInstanceOf[UserDaoProvider]
    daoProvider should not be (null)
    daoProvider.container should not be (null)

    val daoProvider2 = factory.getBean("userDaoProvider").asInstanceOf[UserDaoProvider]
    daoProvider2 should not be (null)

    daoProvider2 should equal(daoProvider)

    val ldapProvider = factory.getBean("userLdapProvider").asInstanceOf[UserLdapProvider]
    ldapProvider should not be (null)

    // userService
    val action = factory.getBean(classOf[SomeAction].getName()).asInstanceOf[SomeAction]

    action should not be (null)

    action.hasDaoProvider() should be(true)
    action.hasLdapProvider() should be(true)

    action.userDaoProvider should equal(daoProvider)
    action.ldapProvider should equal(ldapProvider)

    val action2 = factory.getBean(classOf[SomeAction].getName()).asInstanceOf[SomeAction]

    action2 should not equal (action)
  }
}
