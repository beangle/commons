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
import org.beangle.commons.lang.Strings.{isEmpty, leftPad}
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

  object ToMonthDay extends Converter[String, MonthDay] {
    override def apply(value: String): MonthDay = {
      if isEmpty(value) then null else MonthDay.parse(normalizeMonthDay(value))
    }
  }

  object ToYearMonth extends Converter[String, YearMonth] {
    override def apply(value: String): YearMonth = {
      if isEmpty(value) then null else YearMonth.parse(nomalizeYearMonth(value))
    }
  }

  object ToLocalDate extends Converter[String, LocalDate] {
    override def apply(value: String): LocalDate = {
      if isEmpty(value) then null else LocalDate.parse(Dates.normalize(value))
    }
  }

  object ToLocalDateTime extends Converter[String, LocalDateTime] {
    override def apply(value: String): LocalDateTime = {
      if isEmpty(value) then null else LocalDateTime.parse(normalizeDateTime(value))
    }
  }

  object ToZonedDateTime extends Converter[String, ZonedDateTime] {
    override def apply(value: String): ZonedDateTime = {
      if isEmpty(value) then null else ZonedDateTime.parse(normalizeDateTime(value))
    }
  }

  object ToOffsetDateTime extends Converter[String, OffsetDateTime] {
    override def apply(value: String): OffsetDateTime = {
      if isEmpty(value) then null else OffsetDateTime.parse(normalizeDateTime(value))
    }
  }

  object ToLocalTime extends Converter[String, LocalTime] {
    override def apply(value: String): LocalTime = {
      if isEmpty(value) then null else LocalTime.parse(value)
    }
  }

  object ToInstant extends Converter[String, Instant] {
    override def apply(value: String): Instant = {
      if isEmpty(value) then return null
      if value.endsWith("Z") then Instant.parse(value)
      else LocalDateTime.parse(normalizeDateTime(value)).atZone(ZoneId.systemDefault).toInstant
    }
  }

  /** Change DateTime Format
   *  - YYYY-MM-DD HH:mm into YYYY-MM-DDTHH:mm:00
   *  - YYYY-MM-DD HH:mm:ss into YYYY-MM-DDTHH:mm:ss
   */
  def normalizeDateTime(value: String): String = {
    val v = if (value.length == 16) value + ":00" else value
    Strings.replace(v, " ", "T")
  }

  /** Change YearMonth Format
   *  - YYYY.M into YYYY-0M
   *  - YYYY.MM into YYYY-MM
   *  - YYYY-M into YYYY-0M
   */
  def nomalizeYearMonth(ym: String): String = {
    val str = Strings.replace(ym, ".", "-")
    if (str.contains("-")) {
      val parts = splitDate(str)
      parts(0) + "-" + parts(1)
    } else {
      val parts = splitDate(str.substring(0, 4) + "-" + str.substring(4))
      parts(0) + "-" + parts(1)
    }
  }

  def normalizeMonthDay(md: String): String = {
    var str = Strings.replace(md, ".", "-")
    if str.startsWith("--") then str = str.substring(2)
    val parts = splitDate(str)
    "--" + parts(0) + "-" + parts(1)
  }

  private def splitDate(str: String): Array[String] = {
    val parts = Strings.split(str, "-")
    (0 until parts.length) foreach { i =>
      parts(i) = leftPad(parts(i), 2, '0')
    }
    parts
  }

  register(classOf[LocalDate], ToLocalDate)
  register(classOf[LocalDateTime], ToLocalDateTime)
  register(classOf[ZonedDateTime], ToZonedDateTime)
  register(classOf[OffsetDateTime], ToOffsetDateTime)
  register(classOf[LocalTime], ToLocalTime)
  register(classOf[Instant], ToInstant)
  register(classOf[YearMonth], ToYearMonth)
  register(classOf[MonthDay], ToMonthDay)
}
