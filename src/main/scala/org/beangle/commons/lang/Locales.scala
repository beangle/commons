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

package org.beangle.commons.lang

import java.util.Locale

/** Locale parsing and caching. */
object Locales {

  private var cache = Map("_" -> Locale.getDefault())

  /** Simplified Chinese locale (zh_CN). */
  def chinese: Locale = Locale.SIMPLIFIED_CHINESE

  /** US English locale (en_US). */
  def us: Locale = Locale.US

  /** Parses and caches a locale from string (e.g. "zh_CN", "en-US"). Returns default if blank.
   *
   * @param localeStr the locale string
   * @return the Locale; default if blank
   */
  def of(localeStr: String): Locale = {
    if Strings.isBlank(localeStr) then
      Locale.getDefault()
    else
      cache.get(localeStr) match {
        case None =>
          val n = parse(localeStr)
          cache = cache + (localeStr -> n)
          n
        case Some(locale) => locale
      }
  }

  private def parse(localeStr: String): Locale = {
    val builder = new Locale.Builder
    val parts = Strings.split(localeStr, Array('-', '_'))
    parts.length match {
      case 1 => builder.setLanguage(parts(0)).build()
      case 2 => builder.setLanguage(parts(0)).setRegion(parts(1)).build()
      case _ => builder.setLanguage(parts(0)).setRegion(parts(1)).setVariant(parts(2)).build()
    }
  }
}
