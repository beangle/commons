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

package org.beangle.commons.bean

/** Extracts property value from object by name. */
trait PropertyExtractor {
  /** Gets property value from target.
   *
   * @param target   the object
   * @param property the property name (supports nested path)
   * @return the value
   */
  def get(target: Object, property: String): Any
}

/** PropertyExtractor using Properties.get. */
class DefaultPropertyExtractor extends PropertyExtractor {
  override def get(target: Object, property: String): Any = {
    Properties.get[Any](target, property)
  }
}
