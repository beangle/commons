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
import java.time.{Instant, ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.time.temporal.{Temporal, TemporalAccessor}
import java.util.{Calendar, Date}

trait Formatter {
  def format(obj: Any): String
}

object ToStringFormatter extends Formatter {
  override def format(obj: Any): String = {
    String.valueOf(obj)
  }
}

class NumberFormatter(pattern: String) extends Formatter {
  val df = new DecimalFormat(pattern)
  df.setRoundingMode(RoundingMode.HALF_UP)

  override def format(obj: Any): String = {
    df.format(obj)
  }
}

class DateFormatter(pattern: String) extends Formatter {
  val df = new SimpleDateFormat(pattern)

  override def format(obj: Any): String = {
    df.format(obj)
  }
}

class CalendarFormatter(pattern: String) extends Formatter {
  val df = new SimpleDateFormat(pattern)

  override def format(obj: Any): String = {
    df.format(obj.asInstanceOf[Calendar].getTime)
  }
}

class TemporalFormatter(pattern: String) extends Formatter {
  val df = DateTimeFormatter.ofPattern(pattern)

  override def format(obj: Any): String = {
    df.format(obj.asInstanceOf[TemporalAccessor])
  }
}

class InstantFormatter(pattern: String) extends TemporalFormatter(pattern) {
  override def format(obj: Any): String = {
    df.format(obj.asInstanceOf[Instant].atZone(ZoneId.systemDefault))
  }
}


