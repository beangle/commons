/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.conversion.converter

import org.beangle.commons.lang.Strings
import org.beangle.commons.conversion.Converter

/**
 * Convert String to Boolean.
 * <p>
 * Convert true,on,yes,Y,1 to Boolean.TRUE.<br>
 * Convert false,off,no,N,0,"" to Boolean.FALSE. <br>
 *
 * @author chaostone
 * @since 3.2.0
 */
object String2BooleanConverter extends Converter[String, Boolean] {

  private val trues = Set("true", "on", "Y", "1", "yes")

  override def apply(input: String): Boolean = {
    if (Strings.isEmpty(input)) return false
    if (trues.contains(input.toLowerCase())) true else false
  }
}