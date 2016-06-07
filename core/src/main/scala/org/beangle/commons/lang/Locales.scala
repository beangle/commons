/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang

import java.util.Locale

object Locales {

  def toLocale(localeStr: String): Locale = {
    if (Strings.isBlank(localeStr) || ("_".equals(localeStr)))
      return Locale.getDefault()

    var index = localeStr.indexOf('_')
    if (index < 0) return new Locale(localeStr)

    val language = localeStr.substring(0, index)
    if (index == localeStr.length()) return new Locale(language)

    val new_localeStr = localeStr.substring(index + 1)
    index = new_localeStr.indexOf('_')
    if (index < 0) return new Locale(language, new_localeStr)

    val country = new_localeStr.substring(0, index)
    if (index == new_localeStr.length()) return new Locale(language, country)

    new Locale(language, country, new_localeStr.substring(index + 1))
  }
}