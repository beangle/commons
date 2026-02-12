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

import java.math.RoundingMode
import java.text.{DecimalFormat, SimpleDateFormat}
import java.time.format.DateTimeFormatter
import java.time.temporal.{Temporal, TemporalAccessor}
import java.time.{Instant, ZoneId, ZoneOffset}
import java.util.{Calendar, Date}

/** Formats object to string. */
trait Formatter {
  def format(obj: Any): String
}

/** Uses String.valueOf for formatting. */
object ToStringFormatter extends Formatter {
  override def format(obj: Any): String = {
    String.valueOf(obj)
  }
}

/** Formats numbers with DecimalFormat pattern. */
class NumberFormatter(pattern: String) extends Formatter {
  val df = new DecimalFormat(pattern)
  df.setRoundingMode(RoundingMode.HALF_UP)

  override def format(obj: Any): String = {
    df.format(obj)
  }
}

/** Formats java.util.Date with SimpleDateFormat. */
class DateFormatter(pattern: String) extends Formatter {
  val df = new SimpleDateFormat(pattern)

  override def format(obj: Any): String = {
    df.format(obj)
  }
}

/** Formats Calendar with SimpleDateFormat. */
class CalendarFormatter(pattern: String) extends Formatter {
  val df = new SimpleDateFormat(pattern)

  override def format(obj: Any): String = {
    df.format(obj.asInstanceOf[Calendar].getTime)
  }
}

/** Formats java.time Temporal with DateTimeFormatter. */
class TemporalFormatter(pattern: String) extends Formatter {
  val df = DateTimeFormatter.ofPattern(pattern)

  override def format(obj: Any): String = {
    df.format(obj.asInstanceOf[TemporalAccessor])
  }
}

/** Formats Instant with system timezone. */
class InstantFormatter(pattern: String) extends TemporalFormatter(pattern) {
  override def format(obj: Any): String = {
    df.format(obj.asInstanceOf[Instant].atZone(ZoneId.systemDefault))
  }
}
