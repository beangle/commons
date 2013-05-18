/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.lang.conversion.converter

import org.beangle.commons.lang.conversion.Converter
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Convert Object to String
 *
 * @author chaostone
 * @since 3.2.0
 */
class Object2StringConverter extends Converter[AnyRef, String] {

  override def apply(input: AnyRef): String = {
    if ((input == null)) null else input.toString
  }
}
