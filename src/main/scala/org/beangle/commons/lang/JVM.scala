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
import java.lang.management.ManagementFactory

/** JVM detection (debug mode, server VM, headless, version, GC). */
object JVM {

  /** Returns true if JVM is running with debugger (jdwp). */
  def isDebugMode: Boolean = {
    val args = ManagementFactory.getRuntimeMXBean.getInputArguments
    args.toString.indexOf("-agentlib:jdwp") > 0
  }

  /** Returns true if using Server VM. */
  def isServerMode: Boolean = {
    val name = System.getProperty("java.vm.name")
    name.contains("Server VM")
  }

  /** Returns true if no display (headless). */
  def isHeadless: Boolean = {
    GraphicsEnvironment.isHeadless
  }

  /** Returns java.specification.version. */
  def javaVersion: String = {
    System.getProperty("java.specification.version", "99.0")
  }

  /** Returns the name of the primary garbage collector. */
  def gcName: String = {
    val cs = ManagementFactory.getGarbageCollectorMXBeans
    if cs.isEmpty then "UNKNOWN"
    else
      val ob = cs.get(0).getObjectName.toString
      Strings.substringBetween(ob, "name=", " ")
  }

  /** Returns true if running as GraalVM native image. */
  lazy val isGraal: Boolean = {
    var result = false
    try {
      val nativeImageClazz = Class.forName("org.graalvm.nativeimage.ImageInfo")
      result = java.lang.Boolean.TRUE.equals(nativeImageClazz.getMethod("inImageCode").invoke(null))
    } catch {
      case e: Exception => //ignore
    }
    result || System.getProperty("org.graalvm.nativeimage.imagecode") != null
  }

  /** Returns true if JavaRebel or HotswapAgent is active. */
  lazy val isAgentActive: Boolean = {
    val names = List("org.zeroturnaround.javarebel.Integration",
      "org.zeroturnaround.javarebel.ReloaderFactory",
      "org.hotswap.agent.HotswapAgent")
    isActive(names, null) || isActive(names, JVM.getClass.getClassLoader) || isActive(names, ClassLoader.getSystemClassLoader)
  }

  private def isActive(classNames: List[String], loader: ClassLoader): Boolean = {
    classNames.exists(className => ClassLoaders.exists(className, loader))
  }
}
