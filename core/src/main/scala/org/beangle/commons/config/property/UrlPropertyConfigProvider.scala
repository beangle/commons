/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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

import java.net.URL

import org.beangle.commons.inject.Resources
import org.beangle.commons.io.IOs
import org.beangle.commons.logging.Logging

/**
 * <p>
 * UrlPropertyConfigProvider class.
 * </p>
 *
 * @author chaostone
 */
class UrlPropertyConfigProvider extends PropertyConfig.Provider with Logging {

  var resources: Resources = _

  /**
   * getConfig.
   */
  def getConfig(): collection.Map[String, Any] = {
    try {
      val properties = new collection.mutable.HashMap[String, Any]
      properties ++= IOs.readJavaProperties(resources.global)
      if (null != resources.locals) {
        for (url <- resources.locals) properties ++= IOs.readJavaProperties(url)
      }
      properties ++= IOs.readJavaProperties(resources.user)
      properties
    } catch {
      case e: Exception => {
        logger.error("Exception", e)
        throw new RuntimeException(e)
      }
    }
  }
}
