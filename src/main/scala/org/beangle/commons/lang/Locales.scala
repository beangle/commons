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

object Locales {

  private var cache = Map("_" -> Locale.getDefault())

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
