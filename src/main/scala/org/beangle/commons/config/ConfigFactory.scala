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

package org.beangle.commons.config

import java.util as ju
import scala.jdk.javaapi.CollectionConverters.asScala

/** Config factory (SystemEnvironment, SystemProperties). */
object ConfigFactory {

  /** Config from system environment (env vars; dots mapped to underscores). */
  object SystemEnvironment extends AbstractMapConfig {

    override def getValue(name: String, defaults: Any): Any = {
      var v = System.getenv(name)
      if (null == v) {
        v = System.getenv(toEnvName(name))
      }
      if v == null then defaults else wrap(v)
    }

    private def toEnvName(name: String): String = {
      name.replace('.', '_').toUpperCase
    }

    def keysIterator: Iterator[String] = {
      new EnvPropertyIterator(System.getenv().keySet().iterator())
    }

    override def isResolved: Boolean = true
  }

  private class EnvPropertyIterator(i: ju.Iterator[String]) extends Iterator[String] {
    private def toPropertyName(name: String): String = {
      name.replace('_', '.').toLowerCase
    }

    override def hasNext: Boolean = i.hasNext

    override def next(): String = toPropertyName(i.next)
  }

  /** Config from System.getProperty. */
  object SystemProperties extends AbstractMapConfig {

    def getValue(name: String, defaults: Any): Any = {
      val v = System.getProperty(name)
      if v == null then defaults else v
    }

    def keysIterator: Iterator[String] = {
      asScala(System.getProperties.keys().asInstanceOf[java.util.Enumeration[String]])
    }

    override def isResolved: Boolean = true
  }

}
