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

package org.beangle.commons.lang.text

import org.beangle.commons.lang.time.HourMinute
import org.beangle.commons.lang.{Options, Primitives, Strings}

import java.math.RoundingMode
import java.text.{DecimalFormat, SimpleDateFormat}
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.{lang as jl, util as ju}

object Formatters {

  def format(obj: Any, pattern: String = ""): String = {
    val v = Options.unwrap(obj)
    if null == v then ""
    else
      val formatter = if Strings.isEmpty(pattern) then getDefault(v.getClass) else buildFormatter(v, pattern)
      formatter.format(v)
  }

  def format(obj: Any, formatter: Formatter): String = {
    val v = Options.unwrap(obj)
    if null == v then "" else formatter.format(v)
  }

  private def buildFormatter(v: Any, pattern: String): Formatter = {
    v match {
      case n: Number => NumberFormatter(pattern)
      case t: Temporal => TemporalFormatter(pattern)
      case d: ju.Date => DateFormatter(pattern)
      case c: ju.Calendar => DateFormatter(pattern)
      case _ => ToStringFormatter
    }
  }

  private val PlainNum = NumberFormatter("0")
  private val LongNum = NumberFormatter("#,##0")
  private val DecimalNum = NumberFormatter("#,##0.##")
  private val JuDate = DateFormatter("yyyy-MM-dd")
  private val JuTime = DateFormatter("HH:mm:ss")
  private val JuDateTime = DateFormatter("yyyy-MM-dd HH:mm:ss")
  private val JuCalendarDateTime = CalendarFormatter("yyyy-MM-dd HH:mm:ss")
  private val JtDate = TemporalFormatter("yyyy-MM-dd")
  private val JtTime = TemporalFormatter("HH:mm:ss")
  private val JtDateTime = TemporalFormatter("yyyy-MM-dd HH:mm:ss")
  private val JtOffsetDateTime = TemporalFormatter("yyyy-MM-dd HH:mm:ssXXX")
  private val JtInstant = InstantFormatter("yyyy-MM-dd HH:mm:ssXXX")
  private val JtYearMonth = TemporalFormatter("yyyy-MM")
  private val JtMonthDay = TemporalFormatter("MM-dd")

  val Defaults: Map[Class[_], Formatter] =
    Map(classOf[jl.Boolean] -> ToStringFormatter, classOf[jl.Short] -> PlainNum,
      classOf[jl.Integer] -> PlainNum, classOf[jl.Long] -> LongNum,
      classOf[jl.Float] -> DecimalNum, classOf[jl.Double] -> DecimalNum,
      classOf[java.math.BigDecimal] -> DecimalNum, classOf[scala.math.BigDecimal] -> DecimalNum,
      classOf[java.math.BigInteger] -> LongNum,
      classOf[java.sql.Date] -> JuDate, classOf[java.time.LocalDate] -> JtDate,
      classOf[java.sql.Time] -> JuTime, classOf[java.time.LocalTime] -> JtTime,
      classOf[java.sql.Timestamp] -> JuDateTime, classOf[java.time.LocalDateTime] -> JtDateTime,
      classOf[java.util.Date] -> JuDateTime,
      classOf[java.time.ZonedDateTime] -> JtOffsetDateTime, classOf[java.time.OffsetDateTime] -> JtOffsetDateTime,
      classOf[java.time.Instant] -> JtInstant,
      classOf[java.time.YearMonth] -> JtYearMonth, classOf[java.time.MonthDay] -> JtMonthDay
    )

  def getDefault(clazz: Class[_]): Formatter = {
    val clz = Primitives.wrap(clazz)
    Defaults.get(clz) match {
      case None =>
        if (classOf[java.sql.Date].isAssignableFrom(clz)) JuDate
        else if (classOf[java.util.Date].isAssignableFrom(clz)) JuDateTime
        else if (classOf[java.sql.Timestamp].isAssignableFrom(clz)) JuDateTime
        else if (classOf[java.util.Calendar].isAssignableFrom(clz)) JuCalendarDateTime
        else if (classOf[java.sql.Time].isAssignableFrom(clz)) JuTime
        else ToStringFormatter
      case Some(p) => p
    }
  }
}
