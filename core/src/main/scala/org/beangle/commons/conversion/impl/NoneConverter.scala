/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.conversion.impl

import org.beangle.commons.lang.Objects

/**
 * Convert anything to null.
 *
 * @author chaostone
 * @since 3.2.0
 */
object NoneConverter extends GenericConverter {

  override def convert(input: Any, targetType: Class[_]): Any = {
    Objects.default(targetType)
  }

  override def getTypeinfo: Tuple2[Class[_], Class[_]] = (classOf[AnyRef], classOf[AnyRef])
}
