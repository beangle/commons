/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.lang

import java.io.{File, InputStream}
import java.lang.management.ManagementFactory
import java.net.{URL, URLClassLoader}
import scala.collection.mutable

/** ClassLoaders
 * load class,stream
 */
object ClassLoaders {
  private val buildins = Map("byte" -> classOf[Byte], "Byte" -> classOf[Byte], "boolean" -> classOf[Boolean],
    "Boolean" -> classOf[Boolean], "short" -> classOf[Short], "Short" -> classOf[Short],
    "int" -> classOf[Int], "Int" -> classOf[Int], "long" -> classOf[Long], "Long" -> classOf[Long],
    "char" -> classOf[Char], "Char" -> classOf[Char], "float" -> classOf[Float], "Float" -> classOf[Float],
    "double" -> classOf[Double], "Double" -> classOf[Double], "Integer" -> classOf[Integer], "String" -> classOf[String],
    "void" -> classOf[Unit], "Unit" -> classOf[Unit],
    "Option" -> classOf[Option[_]])

  /** Return the default ClassLoader to use
   * typically the thread context ClassLoader, if available; the ClassLoader that loaded the ClassLoaders
   * class will be used as fallback.
   *
   * @return the default ClassLoader (never <code>null</code>)
   */
  def defaultClassLoader: ClassLoader = {
    var cl =
      try Thread.currentThread().getContextClassLoader
      catch case _: Throwable => null
    if (cl == null) {
      cl = getClass.getClassLoader
      if (null == cl) cl = ClassLoader.getSystemClassLoader
    }
    cl
  }

  /** Find class loader sequence
   */
  private def loaders(callingClass: Class[_] = null): Seq[ClassLoader] = {
    val me = getClass.getClassLoader
    val tl = Thread.currentThread().getContextClassLoader
    val cl = if (null == callingClass) null else callingClass.getClassLoader

    if (null == cl) {
      if (me eq tl) List(tl) else List(tl, me)
    } else if (me eq tl) {
      if (cl eq me) List(tl) else List(tl, cl)
    } else if (me eq cl) {
      List(tl, me)
    } else {
      if (cl == tl) List(tl, me) else List(tl, me, cl)
    }
  }

  /** Load a given resource(Cannot start with slash /).
   */
  def getResource(resourceName: String, callingClass: Class[_] = null): Option[URL] = {
    val path = normalize(resourceName)
    var url: URL = null
    val iter = loaders(callingClass).iterator
    while (null == url && iter.hasNext)
      url = iter.next().getResource(path)
    Option(url)
  }

  /** Load list of resource(Cannot start with slash /).
   *
   * @return List of resources url or empty list.
   */
  def getResources(resourceName: String, callingClass: Class[_] = null): List[URL] = {
    val path = normalize(resourceName)
    var em: java.util.Enumeration[URL] = null
    val iter = loaders(callingClass).iterator
    while ((null == em || !em.hasMoreElements) && iter.hasNext) em = iter.next().getResources(path)

    val urls = new mutable.ListBuffer[URL]
    while (null != em && em.hasMoreElements) urls += em.nextElement
    urls.toList
  }

  /** This is a convenience method to load a resource as a stream.
   *
   * The algorithm used to find the resource is given in getResource()
   */
  def getResourceAsStream(resourceName: String, callingClass: Class[_] = null): Option[InputStream] = {
    getResource(resourceName, callingClass).map { r => r.openStream() }
  }

  def load(className: String, classLoader: ClassLoader = null): Class[_] = {
    val loader = if (classLoader == null) defaultClassLoader else classLoader
    if (buildins.contains(className)) buildins(className)
    else loader.loadClass(className)
  }

  def get(className: String, classLoader: ClassLoader = null): Option[Class[_]] = {
    val loader = if (classLoader == null) defaultClassLoader else classLoader
    if buildins.contains(className) then buildins.get(className)
    else if null != loader.getResource(Strings.replace(className, ".", "/") + ".class") then
      Some(loader.loadClass(className))
    else
      None
  }

  def exists(className: String, classLoader: ClassLoader = null): Boolean = {
    val loader = if (classLoader == null) defaultClassLoader else classLoader
    if buildins.contains(className) then true
    else {
      try {
        Class.forName(className, false, classLoader)
        true
      } catch {
        case var8: Throwable => false
      }
    }
  }

  def urls(loader: ClassLoader): Seq[URL] = {
    loader match
      case ul: URLClassLoader => ul.getURLs.toIndexedSeq
      case _ =>
        ManagementFactory.getRuntimeMXBean.getClassPath.split(File.pathSeparator).toIndexedSeq.map { p =>
          new File(p).toURI.toURL
        }
  }

  private def normalize(resourceName: String): String = {
    if resourceName.charAt(0) == '/' then resourceName.substring(1) else resourceName
  }
}
