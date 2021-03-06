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

import org.beangle.commons.conversion.converter._

object DefaultConversion {

  val Instance = new DefaultConversion()
}

/**
  * Default Conversion implementation.
  * <p>
  * It register String to Boolean/Number/Date/Locale, Number to Number and Object to String buildin
  * converters.
  * @author chaostone
  * @since 3.2.0
  */
class DefaultConversion extends AbstractGenericConversion {

  addConverter(String2BooleanConverter)

  addConverter(String2NumberConverter)

  addConverter(String2DateConverter)

  addConverter(String2TemporalConverter)

  addConverter(String2TimeConverter)

  addConverter(String2HourMinuteConverter)

  addConverter(String2EnumConverter)

  addConverter(String2ScalaEnumConverter)

  addConverter(String2LocaleConverter)

  addConverter(Number2NumberConverter)

  addConverter(Object2StringConverter)

  addConverter(IterableConverterFactory)
}
