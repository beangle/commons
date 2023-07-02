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

import java.awt.GraphicsEnvironment
import java.lang.management.{ManagementFactory, PlatformManagedObject}

object JVM {
  def isDebugMode: Boolean = {
    val args = ManagementFactory.getRuntimeMXBean.getInputArguments
    args.toString.indexOf("-agentlib:jdwp") > 0
  }

  def isServerMode: Boolean = {
    val name = System.getProperty("java.vm.name")
    name.contains("Server VM")
  }

  def isHeadless: Boolean = {
    GraphicsEnvironment.isHeadless
  }

  def javaVersion: String = {
    System.getProperty("java.specification.version", "99.0")
  }

  def gcName: String = {
    val cs = ManagementFactory.getGarbageCollectorMXBeans
    if cs.isEmpty then "UNKNOWN"
    else
      val ob = cs.get(0).getObjectName.toString
      Strings.substringBetween(ob, "name=", " ")
  }

}
