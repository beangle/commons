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
import org.beangle.commons.logging.Logging

/**
 * <p>
 * UrlPropertyConfigProvider class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class UrlPropertyConfigProvider extends PropertyConfig.Provider with Logging {

  var resources: Resources = _

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
      if (null != resources.global) populateConfigItems(properties, resources.global)
      if (null != resources.locals) {
        for (url <- resources.locals) {
          populateConfigItems(properties, url)
        }
      }
      if (null != resources.user) populateConfigItems(properties, resources.user)
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
