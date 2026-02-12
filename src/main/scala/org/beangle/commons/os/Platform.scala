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

package org.beangle.commons.os

import org.beangle.commons.lang.SystemInfo

/** The platform on which the Java process runs. */
object Platform {

  /** OS name from System.getProperty("os.name"). */
  val osName = SystemInfo.os.name

  /** Returns true if running on FreeBSD. */
  def isFreeBSD: Boolean = osName.startsWith("FreeBSD")

  /** Returns true if running on Linux. */
  def isLinux: Boolean = osName.startsWith("Linux")

  /** Returns true if running on macOS. */
  def isMac: Boolean = osName.startsWith("Mac OS X")

  /** Returns true if running on Windows. */
  def isWin: Boolean = osName.startsWith("Windows")
}
