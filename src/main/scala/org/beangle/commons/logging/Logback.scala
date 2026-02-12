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

package org.beangle.commons.logging

import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.{Level, LoggerContext}
import org.beangle.commons.config.Enviroment
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.annotation.beta
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler

import java.io.InputStream

/** Logback logging helper. */
@beta
object Logback {

  /** Configures Logback. In dev mode, uses logback-dev.xml if present. */
  def configure(config: LogConfig): Unit = {
    installJul2Sfl4j()
    var configFile = config.configFile
    if (null == configFile) {
      if (Enviroment.isDevMode) {
        if (null == System.getProperty("logback.configurationFile")) {
          val devFile = getClass.getResource("/logback-dev.xml")
          if null != devFile then configFile = devFile
        }
      }
    }
    if (null != configFile) {
      refreshConfig(configFile.openStream())
    }
    val factory = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    config.levels foreach { case (name, level) =>
      changeLevel(factory, name, level)
    }
  }

  /** Changes logger level. Use "ROOT" for root logger.
   *
   * @param name  logger name or "ROOT"
   * @param level level name (e.g. DEBUG, INFO)
   * @return true if changed
   */
  def changeLevel(name: String, level: String): Boolean = {
    val factory = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    changeLevel(factory, name, level)
  }

  private def changeLevel(factory: LoggerContext, name: String, level: String): Boolean = {
    val loggerName = if name.equalsIgnoreCase("ROOT") then null else name
    val logger = factory.getLogger(loggerName)
    if null != logger then {
      logger.setLevel(Level.valueOf(level))
      true
    } else {
      false
    }
  }

  /** Reloads Logback configuration from the given input stream.
   *
   * @param is the configuration input stream
   * @return the LoggerContext after reload
   */
  def refreshConfig(is: InputStream): LoggerContext = {
    try {
      val lc = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
      lc.reset()
      lc.getStatusManager.clear()

      val configurator = new JoranConfigurator
      configurator.setContext(lc)
      configurator.doConfigure(is)
      lc
    } catch {
      case e: Exception => throw new RuntimeException("Failed to reload Logback configuration", e)
    } finally {
      IOs.close(is)
    }
  }

  /** Installs JUL-to-SLF4J bridge so java.util.logging is routed to SLF4J. */
  def installJul2Sfl4j(): Unit = {
    if (!SLF4JBridgeHandler.isInstalled) {
      SLF4JBridgeHandler.removeHandlersForRootLogger()
      SLF4JBridgeHandler.install()
    }
  }
}
