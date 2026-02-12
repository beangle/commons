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

/** TextResource factory. */
object TextResource {

  /** Empty resource; returns key as-is. */
  object Empty extends TextResource {
    def apply(key: String): Option[String] = Some(key)

    def apply(key: String, defaultValue: String, obj: Any*): String = key

    def locale: Locale = null
  }

}

/** Locale-aware text resource (message bundle).
 *
 * @author chaostone
 */
trait TextResource {

  /** Gets message by key.
   *
   * @param key resource bundle key
   * @return message or None if not found
   */
  def apply(key: String): Option[String]

  /** Gets message by key with MessageFormat args, or default if not found.
   *
   * @param key          resource bundle key
   * @param defaultValue value when not found
   * @param obj          MessageFormat arguments
   * @return formatted message or defaultValue
   */
  def apply(key: String, defaultValue: String, obj: Any*): String

  def locale: Locale
}
