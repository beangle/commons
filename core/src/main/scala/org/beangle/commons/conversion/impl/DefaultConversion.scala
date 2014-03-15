/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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

import org.beangle.commons.conversion.converter._

object DefaultConversion {

  val Instance = new DefaultConversion()
}

import DefaultConversion._
/**
 * Default Conversion implementation.
 * <p>
 * It register String to Boolean/Number/Date/Locale, Number to Number and Object to String buildin
 * converters.
 *
 * @author chaostone
 * @since 3.2.0
 */
class DefaultConversion extends AbstractGenericConversion {

  addConverter(String2BooleanConverter)

  addConverter(new String2NumberConverter())

  addConverter(new String2DateConverter())

  addConverter(new String2EnumConverter())

  addConverter(String2LocaleConverter)

  addConverter(new Number2NumberConverter())

  addConverter(Object2StringConverter)
}
