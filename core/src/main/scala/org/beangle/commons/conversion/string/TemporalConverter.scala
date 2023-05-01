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
    override def apply(value: String): MonthDay = Dates.toMonthDay(value)
  }

  object ToYearMonth extends Converter[String, YearMonth] {
    override def apply(value: String): YearMonth = Dates.toYearMonth(value)
  }

  object ToLocalDate extends Converter[String, LocalDate] {
    override def apply(value: String): LocalDate = Dates.toDate(value)
  }

  object ToLocalDateTime extends Converter[String, LocalDateTime] {
    override def apply(value: String): LocalDateTime = Dates.toDateTime(value)
  }

  object ToZonedDateTime extends Converter[String, ZonedDateTime] {
    override def apply(value: String): ZonedDateTime = Dates.toZonedDateTime(value)
  }

  object ToOffsetDateTime extends Converter[String, OffsetDateTime] {
    override def apply(value: String): OffsetDateTime = Dates.toOffsetateTime(value)
  }

  object ToLocalTime extends Converter[String, LocalTime] {
    override def apply(value: String): LocalTime = Dates.toTime(value)
  }

  object ToInstant extends Converter[String, Instant] {
    override def apply(value: String): Instant = Dates.toInstant(value)
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
