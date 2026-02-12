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

/** TextResource implementation using registry and formatter.
 *
 * @author chaostone
 */
class DefaultTextResource(val locale: Locale, protected val registry: TextBundleRegistry, protected val formatter: TextFormatter) extends TextResource {

  /** If true, use key as default when value not found. */
  var keyAsDefault: Boolean = true

  /** Gets message by key; uses default if not found; formats with args. */
  def apply(key: String, defaultValue: String, args: Any*): String = {
    val text = get(key).getOrElse(if ((null eq defaultValue) && keyAsDefault) key else defaultValue)
    if (null != text) && args.nonEmpty then formatter.format(text, locale, args: _*) else text
  }

  /** Gets message by key; returns Some(key) when keyAsDefault and not found. */
  def apply(key: String): Option[String] = {
    val msg = get(key)
    if (msg.isEmpty && keyAsDefault) Some(key) else msg
  }

  protected def get(key: String): Option[String] = {
    var msg: Option[String] = None
    registry.getBundles(locale) find { bundle =>
      msg = bundle.get(key)
      msg.isDefined
    }
    msg
  }
}
