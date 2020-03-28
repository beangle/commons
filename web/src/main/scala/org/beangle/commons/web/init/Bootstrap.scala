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
package org.beangle.commons.web.init

import java.util.EnumSet
import java.{util, util => ju}

import javax.servlet.DispatcherType.REQUEST
import javax.servlet.{ServletContainerInitializer, ServletContext, ServletContextListener}
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.Strings.{split, substringAfter, substringBefore}
import org.beangle.commons.web.context.ServletContextHolder

object Bootstrap {
  val InitFile = "META-INF/beangle/web-init.properties"
}

/**
  * Web BootstrapListener
  */
class Bootstrap extends ServletContainerInitializer {

  val listeners = new collection.mutable.ListBuffer[ServletContextListener]

  override def onStartup(clazzes: util.Set[Class[_]], servletContext: ServletContext): Unit = {
    ServletContextHolder.store(servletContext)

    val initializers = new ju.LinkedList[Initializer]
    ClassLoaders.getResources(Bootstrap.InitFile) foreach { url =>
      IOs.readJavaProperties(url) get ("initializer") match {
        case Some(clazz) => initializers.add(ClassLoaders.load(clazz).getDeclaredConstructor().newInstance().asInstanceOf[Initializer])
        case None =>
      }
    }

    if (initializers.isEmpty) {
      servletContext.log("No Beangle Initializer types detected on classpath")
    } else {
      import scala.jdk.CollectionConverters._
      for (initializer <- initializers.asScala) {
        initializer.boss = this
        servletContext.log(s"${initializer.getClass.getName} registering ...")
        initializer.onStartup(servletContext)
      }

      //process filter order
      val filterOrders = servletContext.getInitParameter("filter-orders")
      if (null != filterOrders) {
        val orders = split(filterOrders, ";")
        orders foreach { order =>
          val pattern = substringBefore(order, "=")
          split(substringAfter(order, "="), ",") foreach { filterName =>
            val fr = servletContext.getFilterRegistration(filterName)
            if (null == fr) sys.error(s"Cannot find filter $filterName")
            fr.addMappingForUrlPatterns(EnumSet.of(REQUEST), true, pattern)
          }
        }
      }

      //register each listener
      listeners foreach { listener =>
        servletContext.addListener(listener)
      }
    }
  }

  def addListener(other: ServletContextListener): Unit = {
    listeners += other
  }
}
