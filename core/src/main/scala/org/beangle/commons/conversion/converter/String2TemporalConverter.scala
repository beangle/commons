/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
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
package org.beangle.commons.conversion.converter

import java.time.{ Instant, LocalDate, LocalDateTime, LocalTime, ZoneId, ZonedDateTime }
import java.time.temporal.Temporal

import org.beangle.commons.conversion.Converter
import org.beangle.commons.lang.{ Dates, Strings }
import org.beangle.commons.lang.Strings.isEmpty

/**
 * DateConverter
 *
 * @author chaostone
 * @since 3.2.0
 */
class String2TemporalConverter extends StringConverterFactory[String, Temporal] {

  register(classOf[LocalDate], LocalDateConverter)

  register(classOf[LocalDateTime], LocalDateTimeConverter)

  register(classOf[LocalTime], TimeConverter)

  register(classOf[Instant], InstantConverter)

  object LocalDateConverter extends Converter[String, LocalDate] {
    override def apply(value: String): LocalDate = {
      if (isEmpty(value)) return null
      LocalDate.parse(Dates.normalize(value))
    }
  }

  object LocalDateTimeConverter extends Converter[String, LocalDateTime] {
    override def apply(value: String): LocalDateTime = {
      if (isEmpty(value)) return null
      LocalDateTime.parse(normalize(value))
    }
  }

  object ZonedDateTimeConverter extends Converter[String, ZonedDateTime] {
    override def apply(value: String): ZonedDateTime = {
      if (isEmpty(value)) return null
      ZonedDateTime.parse(value)
    }
  }

  object TimeConverter extends Converter[String, LocalTime] {
    override def apply(value: String): LocalTime = {
      if (isEmpty(value)) return null
      LocalTime.parse(value)
    }
  }

  object InstantConverter extends Converter[String, Instant] {
    override def apply(value: String): Instant = {
      if (isEmpty(value)) return null
      if (!value.endsWith("Z")) {
        LocalDateTime.parse(normalize(value)).atZone(ZoneId.systemDefault).toInstant
      } else {
        Instant.parse(value)
      }
    }
  }

  /**
   * Change DateTime Format
   * 1. YYYY-MM-DD HH:mm into YYYY-MM-DDTHH:mm:00
   * 2. YYYY-MM-DD HH:mm:ss into YYYY-MM-DDTHH:mm:ss
   */
  def normalize(value: String): String = {
    val v = if (value.length == 16) value + ":00" else value
    Strings.replace(v, " ", "T")
  }
}
