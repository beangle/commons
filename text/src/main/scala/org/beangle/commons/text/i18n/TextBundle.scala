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

package org.beangle.commons.text.i18n

import java.util.Locale

/** TextBundle
  *
  * @author chaostone
  * @since 3.0.0
  */
final class TextBundle(val locale: Locale, val resource: String, val texts: Map[String, String]) {

  def get(key: String): Option[String] = texts.get(key)

  override def toString: String = resource

  def merge(second: TextBundle): TextBundle = {
    val values = this.texts ++ second.texts
    new TextBundle(this.locale, this.resource + ";" + second, values)
  }
}
