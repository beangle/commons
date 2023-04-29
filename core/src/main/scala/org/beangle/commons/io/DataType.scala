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

package org.beangle.commons.io

import org.beangle.commons.lang.{Numbers, Primitives}

enum DataType {
  case String, Boolean, Short, Integer, Long, Float, Double, Date, Time, DateTime, YearMonth, MonthDay, OffsetDateTime, Instant, Formula, Blank, Error
}

object DataType {
  def toType(clazz: Class[_]): DataType = {
    val clz = Primitives.wrap(clazz)
    if (classOf[java.lang.Boolean].isAssignableFrom(clz)) {
      Boolean
    } else if (classOf[java.lang.Short].isAssignableFrom(clz)) {
      Short
    } else if (classOf[java.lang.Integer].isAssignableFrom(clz)) {
      Integer
    } else if (classOf[java.lang.Long].isAssignableFrom(clz)) {
      Long
    } else if (classOf[java.lang.Float].isAssignableFrom(clz)) {
      Float
    } else if (classOf[java.lang.Double].isAssignableFrom(clz)) {
      Double
    } else if (classOf[java.sql.Date].isAssignableFrom(clz) || classOf[java.time.LocalDate].isAssignableFrom(clz)) {
      Date
    } else if (classOf[java.sql.Time].isAssignableFrom(clz) || classOf[java.time.LocalTime].isAssignableFrom(clz)) {
      Time
    } else if (classOf[java.sql.Timestamp].isAssignableFrom(clz) || classOf[java.util.Date].isAssignableFrom(clz)
      || classOf[java.time.LocalDateTime].isAssignableFrom(clz)) {
      DateTime
    } else if (classOf[java.time.ZonedDateTime].isAssignableFrom(clz) || classOf[java.time.OffsetDateTime].isAssignableFrom(clz)) {
      OffsetDateTime
    } else if (classOf[java.time.Instant].isAssignableFrom(clz)) {
      Instant
    } else if (classOf[java.time.YearMonth].isAssignableFrom(clz)) {
      YearMonth
    } else if (classOf[java.time.MonthDay].isAssignableFrom(clz)) {
      MonthDay
    } else {
      String
    }
  }

}
