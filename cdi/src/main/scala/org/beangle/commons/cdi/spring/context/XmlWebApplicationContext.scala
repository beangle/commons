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

import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.xml.{ ResourceEntityResolver, XmlBeanDefinitionReader }
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext
import org.beangle.commons.event.EventMulticaster

class XmlWebApplicationContext extends AbstractRefreshableConfigApplicationContext {

  protected override def loadBeanDefinitions(beanFactory: DefaultListableBeanFactory) {
    // Create a new XmlBeanDefinitionReader for the given BeanFactory.
    val reader = new XmlBeanDefinitionReader(beanFactory)
    // Configure the bean definition reader with this context's resource loading environment.
    reader.setEnvironment(this.getEnvironment)
    reader.setResourceLoader(this)
    reader.setEntityResolver(new ResourceEntityResolver(this))
    reader.setValidating(false)
    val configLocations = getConfigLocations
    if (configLocations != null) configLocations foreach (cl => reader.loadBeanDefinitions(cl))
  }

  /**
   * publish beangle BeanFactoryRefreshedEvent
   */
  protected override def finishRefresh(): Unit = {
    super.finishRefresh()
    val eventMulticasterIter = this.getBeansOfType(classOf[EventMulticaster]).values.iterator()
    if (eventMulticasterIter.hasNext()) {
      val eventMulticaster = eventMulticasterIter.next()
      eventMulticaster.multicast(new BeanFactoryRefreshedEvent(this.getBeanFactory))
    }
  }
}
