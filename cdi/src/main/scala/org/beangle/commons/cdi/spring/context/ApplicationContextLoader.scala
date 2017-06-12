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
package org.beangle.commons.cdi.spring.context

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.{ ApplicationContext, ConfigurableApplicationContext }
import org.springframework.context.support.{ AbstractRefreshableApplicationContext, AbstractRefreshableConfigApplicationContext }

/**
 * Load ApplicationContext
 */
class ApplicationContextLoader extends ContextLoader with Logging {

  var result: ConfigurableApplicationContext = _

  private def determineContextClass(ctxClassName: String): Class[_] = {
    if (ctxClassName != null) {
      ClassLoaders.load(ctxClassName)
    } else {
      ClassLoaders.getResource("org/springframework/web/context/ContextLoader.properties") match {
        case Some(propUrl) =>
          val properties = IOs.readJavaProperties(propUrl)
          ClassLoaders.load(properties("org.springframework.web.context.WebApplicationContext"))
        case None =>
          classOf[XmlWebApplicationContext]
      }
    }
  }

  def load(id: String, contextClassName: String, configLocation: String, parent: BeanFactory): ApplicationContext = {
    val contextClass = determineContextClass(contextClassName)
    require(classOf[ConfigurableApplicationContext].isAssignableFrom(contextClass))
    val watch = new Stopwatch(true)
    logger.info(s"$id starting")
    result = Reflections.newInstance(contextClass).asInstanceOf[ConfigurableApplicationContext]
    result match {
      case ara: AbstractRefreshableApplicationContext => ara.setAllowBeanDefinitionOverriding(false)
      case _ =>
    }
    result.setId(id)
    result.setParent(parent.asInstanceOf[ApplicationContext])
    result.asInstanceOf[AbstractRefreshableConfigApplicationContext].setConfigLocation(configLocation)
    result.refresh()
    logger.info(s"$id started in $watch")
    result
  }

  override def close(): Unit = {
    result.close()
  }
}
