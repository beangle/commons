/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.i18n

import java.util.Locale

/**
 * TextBundle
 *
 * @author chaostone
 * @since 3.0.0
 */
trait TextBundle {

  /**
   * Gets a message based on a message key, or null if no message is found.
   */
  def get(key: String): Option[String]

  /**
   * Returns the locale of this resource bundle.
   */
  def locale: Locale

  /**
   * Get the bundle resource path
   */
  def resource: String
}
