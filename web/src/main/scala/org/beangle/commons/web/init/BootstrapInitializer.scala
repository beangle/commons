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
package org.beangle.commons.web.init

import java.lang.reflect.Modifier
import java.{ util => ju }
import javax.servlet.ServletContainerInitializer
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.annotation.HandlesTypes
import scala.collection.JavaConversions._
/**
 * Web BootstrapInitializer
 */
@HandlesTypes(Array(classOf[Initializer]))
class BootstrapInitializer extends ServletContainerInitializer {
  def onStartup(initializerClasses: ju.Set[Class[_]], servletContext: ServletContext) {
    val initializers = new ju.LinkedList[Initializer]
    if (initializerClasses != null) {
      for (clazz <- initializerClasses) {
        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())
          && classOf[Initializer].isAssignableFrom(clazz)) {
          try {
            initializers.add(clazz.newInstance().asInstanceOf[Initializer]);
          } catch {
            case ex: Throwable => throw new ServletException("Failed to instantiate StartupInitializer class", ex);
          }
        }
      }
    }

    if (initializers.isEmpty) {
      servletContext.log("No Beangle Initializer types detected on classpath")
    } else {
      servletContext.log("Beangle Initializer detected on classpath: " + initializers);
      for (initializer <- initializers) initializer.onStartup(servletContext)
    }
  }
}
