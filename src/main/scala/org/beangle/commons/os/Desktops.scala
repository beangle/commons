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

import java.net.URI

object Desktops {

  def openBrowser(url: String): Unit = {
    try {
      val desktopClass = Class.forName("java.awt.Desktop")
      val supported = desktopClass.getMethod("isDesktopSupported").invoke(null, Array.empty[Object]).asInstanceOf[Boolean]
      val uri = new URI(url)
      if (supported) {
        val desktop = desktopClass.getMethod("getDesktop").invoke(null)
        desktopClass.getMethod("browse", classOf[URI]).invoke(desktop, uri)
        return
      }
    } catch {
      case _: Throwable =>
    }
    val osName = System.getProperty("os.name").toLowerCase
    val rt = Runtime.getRuntime
    if (osName.contains("windows")) {
      rt.exec(Array("rundll32", "url.dll,FileProtocolHandler", url))
    } else if (osName.contains("mac") || osName.contains("darwin")) {
      Runtime.getRuntime.exec(Array("open", url))
    } else {
      val browsers = Array("xdg-open", "chromium", "google-chrome", "firefox", "konqueror", "netscape", "opera", "midori")
      var ok = false
      for (b <- browsers; if !ok) {
        try {
          rt.exec(Array(b, url))
          ok = true
        } catch {
          case _: Throwable =>
        }
      }
      if (!ok) throw new Exception("Cannot open browser.")
    }
  }
}
