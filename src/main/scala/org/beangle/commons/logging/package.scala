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

package org.beangle.commons

import org.slf4j.LoggerFactory

/** Logging helpers (SLF4J logger by name or class). */
package object logging {

  /** Gets an SLF4J logger by name.
   *
   * @param name logger name
   * @return logger instance
   */
  def slf4j(name: String): org.slf4j.Logger = LoggerFactory.getLogger(name)

  /** Gets an SLF4J logger for the given class.
   *
   * @param clazz class used for logger name
   * @return logger instance
   */
  def slf4j(clazz: Class[_]): org.slf4j.Logger = LoggerFactory.getLogger(clazz)
}
