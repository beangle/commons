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

import java.{ util => ju }
import java.util.EnumSet
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.Strings.{ split, substringAfter, substringBefore }
import javax.servlet.{ ServletContextEvent, ServletContextListener, ServletException }
import javax.servlet.DispatcherType.REQUEST
import org.beangle.commons.web.context.ServletContextHolder

object BootstrapListener {
  val InitFile = "META-INF/beangle/web-init.properties"
}

/**
 * Web BootstrapListener
 */
class BootstrapListener extends ServletContextListener {

  import BootstrapListener._
  val others = new collection.mutable.ListBuffer[ServletContextListener]

  override def contextInitialized(sce: ServletContextEvent): Unit = {
    val servletContext = sce.getServletContext
    ServletContextHolder.store(servletContext)

    val initializers = new ju.LinkedList[Initializer]
    ClassLoaders.getResources(InitFile) foreach { url =>
      IOs.readJavaProperties(url) get ("initializer") match {
        case Some(clazz) => initializers.add(ClassLoaders.load(clazz).getDeclaredConstructor().newInstance().asInstanceOf[Initializer])
        case None        =>
      }
    }

    if (initializers.isEmpty) {
      servletContext.log("No Beangle Initializer types detected on classpath")
    } else {
      for (initializer <- scala.jdk.CollectionConverters.asScala(initializers)) {
        initializer.boss = this
        sce.getServletContext.log(s"${initializer.getClass.getName} registering ...")
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

      // run listener
      others foreach { listener =>
        listener.contextInitialized(sce)
      }
    }
  }

  override def contextDestroyed(sce: ServletContextEvent): Unit = {
    others foreach { listener =>
      listener.contextDestroyed(sce)
    }
  }

  def addListener(other: ServletContextListener): Unit = {
    others += other
  }
}
