/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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

import java.io.{ IOException, InputStream }
import java.net.URL
import java.util.Enumeration

import scala.collection.mutable
/**
 * ClassLoaders
 */
object ClassLoaders {
  private val buildins = Map("Byte" -> classOf[Byte], "Boolean" -> classOf[Boolean], "Short" -> classOf[Short],
    "Int" -> classOf[Int], "Long" -> classOf[Long], "Char" -> classOf[Char], "String" -> classOf[String],
    "Float" -> classOf[Float], "Double" -> classOf[Double])
  /**
   * Return the default ClassLoader to use: typically the thread context
   * ClassLoader, if available; the ClassLoader that loaded the ClassLoaders
   * class will be used as fallback.
   *
   * @return the default ClassLoader (never <code>null</code>)
   */
  def defaultClassLoader: ClassLoader = {
    var cl = try {
      Thread.currentThread().getContextClassLoader
    } catch {
      case ex: Throwable => null
    }
    if (cl == null) {
      cl = getClass.getClassLoader
      if (null == cl) cl = ClassLoader.getSystemClassLoader()
    }
    cl
  }

  /**
   * Find class loader sequence
   */
  private def loaders(callingClass: Class[_] = null): Seq[ClassLoader] = {
    val me = getClass.getClassLoader
    val threadCl = Thread.currentThread().getContextClassLoader
    val callCl = if (null == callingClass) null else callingClass.getClassLoader
    if (null == callCl) {
      if (me == threadCl) List(threadCl) else List(threadCl, me)
    } else {
      if (me == threadCl) {
        if (callCl == me) List(threadCl) else List(threadCl, callCl)
      } else {
        if (callCl == me) {
          List(threadCl, me)
        } else {
          if (callCl == threadCl) List(threadCl, me) else List(threadCl, me, callCl)
        }
      }
    }
  }
  /**
   * Load a given resource(Cannot start with slash /).
   */
  def getResource(resourceName: String, callingClass: Class[_] = null): URL = {
    var url: URL = null
    val iter = loaders(callingClass).iterator
    while (null == url && iter.hasNext)
      url = iter.next().getResource(resourceName)
    url
  }

  /**
   * Load list of resource(Cannot start with slash /).
   * @return List of resources url or empty list.
   */
  def getResources(resourceName: String, callingClass: Class[_] = null): List[URL] = {
    var em: Enumeration[URL] = null
    val iter = loaders(callingClass).iterator
    while ((null == em || !em.hasMoreElements) && iter.hasNext)
      em = iter.next().getResources(resourceName)

    val urls = new mutable.ListBuffer[URL]
    while (null != em && em.hasMoreElements) urls += em.nextElement
    urls.toList
  }

  /**
   * This is a convenience method to load a resource as a stream.
   * The algorithm used to find the resource is given in getResource()
   */
  def getResourceAsStream(resourceName: String, callingClass: Class[_] = null): InputStream = {
    val url = getResource(resourceName, callingClass)
    try {
      if ((url != null)) url.openStream() else null
    } catch {
      case e: IOException => null
    }
  }

  def loadClass(className: String, classLoader: ClassLoader = null): Class[_] = {
    val loader = if (classLoader == null) defaultClassLoader else classLoader
    if (buildins.contains(className)) buildins(className) else loader.loadClass(if (className.contains(".")) className else "java.lang." + className)
  }

  def newInstance[T](className: String, classLoader: ClassLoader = null): T = {
    loadClass(className, classLoader).newInstance().asInstanceOf[T]
  }
}
