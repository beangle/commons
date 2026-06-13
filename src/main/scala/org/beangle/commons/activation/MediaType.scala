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

package org.beangle.commons.activation

/** MIME type (primary/subtype, e.g. text/plain). */
case class MediaType(primaryType: String, subType: String) {

  def this(pt: String) = {
    this(pt, "*")
  }

  override def toString: String =
    if subType == "*" then primaryType else primaryType + "/" + subType
}

/** MediaType factory. */
object MediaType {

  /** Creates MediaType from token (e.g. "text/plain" or "text/plain;charset=utf-8").
   *
   * @param token MIME type string, optionally with params
   * @return MediaType
   */
  def apply(token: String): MediaType = {
    val commaIndex = token.indexOf(";")
    val mimetype = if commaIndex > -1 then token.substring(0, commaIndex).trim else token.trim
    val slashIndex = mimetype.indexOf("/")
    if slashIndex == -1 then
      MediaType(mimetype, "*")
    else
      MediaType(mimetype.substring(0, slashIndex), mimetype.substring(slashIndex + 1))
  }
}
