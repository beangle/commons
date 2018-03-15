/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.config.property

import java.net.URL

import org.beangle.commons.config.Resources
import org.beangle.commons.io.IOs

/**
 * <p>
 * UrlPropertyConfigProvider class.
 * </p>
 *
 * @author chaostone
 */
class UrlPropertyConfigProvider extends PropertyConfig.Provider {

  var resources: Resources = _

  /**
   * getConfig.
   */
  def getConfig(): collection.Map[String, Any] = {
    val properties = new collection.mutable.HashMap[String, Any]
    resources.paths foreach { p =>
      properties ++= IOs.readJavaProperties(p)
    }
    properties
  }
}
