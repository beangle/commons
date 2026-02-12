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

package org.beangle.commons.io

import org.beangle.commons.lang.{ClassLoaders, Strings}
import org.beangle.commons.regex.AntPathPattern
import org.beangle.commons.regex.AntPathPattern.isPattern

import java.io.File
import java.lang.reflect.Method
import java.net.{JarURLConnection, URL}
import java.util.jar.JarFile

/** Resolves resources by location pattern (supports Ant-style patterns). */
trait ResourceResolver {

  /** Prefix for loading all matching classpath resources. */
  val ClasspathAllUrlPrefix = "classpath*:"
  /** Prefix for loading a single classpath resource. */
  val ClasspathUrlPrefix = "classpath:"

  /** Returns all resources matching the location pattern.
   *
   * @param locationPattern the pattern (e.g. classpath&#42;:META-INF/&#42;&#42;)
   * @return list of matching URLs
   */
  def getResources(locationPattern: String): List[URL]
}
