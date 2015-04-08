/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.commons.conversion.impl

import org.beangle.commons.conversion.Converter

/**
 * Adapte a Converter to GenericConverter
 *
 * @author chaostone
 * @since 3.2.0
 */
class ConverterAdapter(iconverter: Converter[_, _], typeinfo: Tuple2[Class[_], Class[_]])
    extends GenericConverter() {

  val converter = iconverter.asInstanceOf[Converter[Any, Any]]

  override def convert(input: Any, targetType: Class[_]): Any = converter.apply(input)

  override def getTypeinfo(): Tuple2[Class[_], Class[_]] = typeinfo
}
