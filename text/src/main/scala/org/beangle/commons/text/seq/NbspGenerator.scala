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

package org.beangle.commons.text.seq

/** NbspGenerator class.
  *
  * @author chaostone
  */
object NbspGenerator {

  /** generator.
    *
    * @param repeat a int.
    * @return a String object.
    */
  def generator(repeat: Int): String = {
    val repeater = "&nbsp;"
    val returnval = new StringBuilder()
    for (i <- 0 until repeat)
      returnval.append(repeater)
    returnval.toString
  }
}
