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
import org.beangle.commons.lang.Strings.isEmpty
import org.beangle.commons.lang.{Dates, Strings}

import java.time.*
import java.time.temporal.Temporal

/**
  * DateConverter
  *
  * @author chaostone
  * @since 3.2.0
  */
object TemporalConverter extends StringConverterFactory[String, Temporal] {

  private object YearMonthConverter extends Converter[String, YearMonth] {
    override def apply(value: String): YearMonth = {
      if isEmpty(value) then null else YearMonth.parse(value)
    }
  }

  private object LocalDateConverter extends Converter[String, LocalDate] {
    override def apply(value: String): LocalDate = {
      if isEmpty(value) then null else LocalDate.parse(Dates.normalize(value))
    }
  }

  private object LocalDateTimeConverter extends Converter[String, LocalDateTime] {
    override def apply(value: String): LocalDateTime = {
      if isEmpty(value) then null else LocalDateTime.parse(normalize(value))
    }
  }

  private object ZonedDateTimeConverter extends Converter[String, ZonedDateTime] {
    override def apply(value: String): ZonedDateTime = {
      if isEmpty(value) then null else ZonedDateTime.parse(value)
    }
  }

  private object TimeConverter extends Converter[String, LocalTime] {
    override def apply(value: String): LocalTime = {
      if isEmpty(value) then null else LocalTime.parse(value)
    }
  }

  private object InstantConverter extends Converter[String, Instant] {
    override def apply(value: String): Instant = {
      if isEmpty(value) then return null
      if value.endsWith("Z") then Instant.parse(value)
      else LocalDateTime.parse(normalize(value)).atZone(ZoneId.systemDefault).toInstant
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

  register(classOf[LocalDate], LocalDateConverter)

  register(classOf[LocalDateTime], LocalDateTimeConverter)

  register(classOf[LocalTime], TimeConverter)

  register(classOf[Instant], InstantConverter)

  register(classOf[YearMonth], YearMonthConverter)
}
