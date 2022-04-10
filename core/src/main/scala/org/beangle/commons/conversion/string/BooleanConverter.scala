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

package org.beangle.commons.conversion.string

import org.beangle.commons.conversion.Converter
import org.beangle.commons.lang.Strings

import java.lang as jl

/**
  * Convert String to Boolean.
  * <p>
  * Convert true,on,yes,Y,1 to Boolean.TRUE.<br>
  * Convert false,off,no,N,0,"" to Boolean.FALSE. <br>
  *
  * @author chaostone
  * @since 3.2.0
  */
object BooleanConverter extends Converter[String, jl.Boolean] {

  private val trues = Set("true", "on", "Y", "1", "yes", "t", "y", "是")

  override def apply(input: String): jl.Boolean = {
    if Strings.isEmpty(input) then false
    else trues.contains(input.toLowerCase())
  }
}
