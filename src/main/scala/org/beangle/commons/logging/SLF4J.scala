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

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import org.beangle.commons.cdi.EnvProfile
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.annotation.beta
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler

import java.io.InputStream

@beta
object SLF4J {

  /** 在开发环境中，启用logback-dev.xml(如果存在)
   */
  def enableLogbackDevConfig(): Unit = {
    if (EnvProfile.devEnabled) {
      if (null == System.getProperty("logger.configurationFile")) {
        val devFile = getClass.getResource("/logback-dev.xml")
        if (null != devFile) {
          reloadLogbackConfig(devFile.openStream())
        }
      }
    }
  }

  /** 重新加载logback配置
   *
   * @param is
   * @return
   */
  def reloadLogbackConfig(is: InputStream): Boolean = {
    try {
      val lc = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
      lc.reset()
      val configurator = new JoranConfigurator
      configurator.setContext(lc)
      configurator.doConfigure(is)
      true
    } catch {
      case e: Exception => throw new RuntimeException("重新加载 Logback 配置失败", e)
    } finally {
      IOs.close(is)
    }
  }

  /** 显式安装java logging到slf4j的桥接
   */
  def installJul2Sfl4j(): Unit = {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }
}
