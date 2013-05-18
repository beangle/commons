/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.config.property

import java.io.InputStream
import java.net.URL
import java.util.Properties
import org.beangle.commons.inject.Resources
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import UrlPropertyConfigProvider._
import scala.beans.BeanProperty

object UrlPropertyConfigProvider {

  /**
   Constant <code>logger</code>
   */
  protected val logger = LoggerFactory.getLogger(classOf[UrlPropertyConfigProvider])
}

/**
 * <p>
 * UrlPropertyConfigProvider class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class UrlPropertyConfigProvider extends PropertyConfig.Provider {

  @BeanProperty
  protected var resources: Resources = _

  /**
   * <p>
   * getConfig.
   * </p>
   *
   * @return a {@link java.util.Properties} object.
   */
  def getConfig(): Properties = {
    try {
      val properties = new Properties()
      if (null != resources.getGlobal) populateConfigItems(properties, resources.getGlobal)
      if (null != resources.getLocals) {
        for (url <- resources.getLocals) {
          populateConfigItems(properties, url)
        }
      }
      if (null != resources.getUser) populateConfigItems(properties, resources.getUser)
      properties
    } catch {
      case e: Exception => {
        logger.error("Exception", e)
        throw new RuntimeException(e)
      }
    }
  }

  private def populateConfigItems(properties: Properties, url: URL) {
    logger.debug("loading {}", url)
    try {
      val is = url.openStream()
      properties.load(is)
      is.close()
    } catch {
      case e: Exception => logger.error("populate config error", e)
    }
  }

}
