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

package org.beangle.commons.conversion.impl

import org.beangle.commons.conversion.{Conversion, converter, string}

/** Built-in conversion singleton.
 *
 * Registers default String/Number/Date/Enum/JSON converters via `ConverterRegistry`,
 * then wraps the frozen registry in `GenericConversion`.
 */
object DefaultConversion {

  val Instance: Conversion = build()

  private def build(): Conversion = {
    val registry = new ConverterRegistry()
    registry.add(string.BooleanConverter)
    registry.add(string.NumberConverters)
    registry.add(string.DateConverter)
    registry.add(string.TemporalConverter)
    registry.add(string.TimeConverter)
    registry.add(string.JavaEnumConverters)
    registry.add(string.EnumConverters)
    registry.add(string.LocaleConverter)
    registry.add(string.ToStringConverter)
    registry.add(string.JsonConverter)
    registry.add(string.DurationConverter)
    registry.add(converter.Number2NumberConverter)
    registry.add(converter.IterableConverterFactory)
    registry.add(converter.DateTimeConverterFactory)
    new GenericConversion(registry.build())
  }
}
