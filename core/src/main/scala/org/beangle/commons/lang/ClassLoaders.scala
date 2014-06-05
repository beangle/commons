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
package org.beangle.commons.lang

import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.Enumeration
import scala.collection.mutable
/**
 * ClassLoaders
 */
object ClassLoaders {

  /**
   * Return the default ClassLoader to use: typically the thread context
   * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
   * class will be used as fallback.
   * <p>
   * Call this method if you intend to use the thread context ClassLoader in a scenario where you
   * absolutely need a non-null ClassLoader reference: for example, for class path resource loading
   * (but not necessarily for <code>Class.forName</code>, which accepts a <code>null</code>
   * ClassLoader reference as well).
   *
   * @return the default ClassLoader (never <code>null</code>)
   * @see java.lang.Thread#getContextClassLoader()
   */
  def getDefaultClassLoader(): ClassLoader = {
    var cl: ClassLoader = null
    try {
      cl = Thread.currentThread().getContextClassLoader
    } catch {
      case ex: Throwable =>
    }
    if (cl == null) cl = this.getClass().getClassLoader
    cl
  }

  /**
   * Load a given resource(Cannot start with slash /).
   * <p/>
   * This method will try to load the resource using the following methods (in order):
   * <ul>
   * <li>From {@link Thread#getContextClassLoader() Thread.currentThread().getContextClassLoader()}
   * <li>From {@link Class#getClassLoader() ClassLoaders.class.getClassLoader()}
   * <li>From the {@link Class#getClassLoader() callingClass.getClassLoader() }
   * </ul>
   *
   * @param resourceName The name of the resource to load
   * @param callingClass The Class object of the calling object
   */
  def getResource(resourceName: String, callingClass: Class[_] = this.getClass): URL = {
    var url = Thread.currentThread().getContextClassLoader.getResource(resourceName)
    if (url != null) return url
    url = this.getClass.getClassLoader.getResource(resourceName)
    if (url != null) return url
    if (callingClass != this.getClass()) {
      val cl = callingClass.getClassLoader
      if (cl != null) url = cl.getResource(resourceName)
    }
    url
  }

  /**
   * Load list of resource(Cannot start with slash /).
   * <p/>
   * This method will try to load the resource using the following methods (in order):
   * <ul>
   * <li>From {@link Thread#getContextClassLoader() Thread.currentThread().getContextClassLoader()}
   * <li>From {@link Class#getClassLoader() ClassLoaders.class.getClassLoader()}
   * <li>From the {@link Class#getClassLoader() callingClass.getClassLoader() }
   * </ul>
   *
   * @param resourceName
   * @param callingClass
   * @return List of resources url or empty list.
   */
  def getResources(resourceName: String, callingClass: Class[_] = this.getClass): List[URL] = {
    var em: Enumeration[URL] = null
    try {
      em = Thread.currentThread().getContextClassLoader.getResources(resourceName)
      if (!em.hasMoreElements()) {
        em = this.getClass.getClassLoader.getResources(resourceName)
        if (!em.hasMoreElements() && callingClass != this.getClass) {
          val cl = callingClass.getClassLoader
          if (cl != null) em = cl.getResources(resourceName)
        }
      }
    } catch {
      case e: IOException => e.printStackTrace()
    }
    val urls = new mutable.ListBuffer[URL]
    while (null != em && em.hasMoreElements()) urls += em.nextElement
    urls.toList
  }

  /**
   * This is a convenience method to load a resource as a stream.
   * The algorithm used to find the resource is given in getResource()
   *
   * @param resourceName The name of the resource to load
   * @param callingClass The Class object of the calling object
   */
  def getResourceAsStream(resourceName: String, callingClass: Class[_] = this.getClass): InputStream = {
    val url = getResource(resourceName, callingClass)
    try {
      if ((url != null)) url.openStream() else null
    } catch {
      case e: IOException => null
    }
  }

  def loadClass(className: String): Class[_] = Class.forName(className)
}
