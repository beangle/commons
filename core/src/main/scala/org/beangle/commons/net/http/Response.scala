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

package org.beangle.commons.net.http

import java.net.HttpURLConnection

case class Response(status: Int, content: Any) {

  def getText: String =
    String.valueOf(content)

  def isOk: Boolean =
    status == HttpURLConnection.HTTP_OK

  def getOrElse(default: => String): String =
    if (this.isOk)
      String.valueOf(content)
    else
      default
}
