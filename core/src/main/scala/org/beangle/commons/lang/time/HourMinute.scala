/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.commons.lang.time
import org.beangle.commons.lang.Numbers.toShort
/**
 * Hour and minute of day
 * @version 4.0.5
 * @since 4.0.5
 */
object HourMinute {
  def apply(time: String): HourMinute = {
    new HourMinute(convert(time))
  }

  def convert(time: String): Short = {
    var index = time.indexOf(':')
    require(index == 2 && time.length == 5, "illegal time,it should with 00:00 format")
    require((toShort(time.substring(0, index)) < 60 && toShort(time.substring(index + 1, index + 3)) < 60),
      s"illegal time $time,it should within 60:60.")
    toShort(time.substring(0, index) + time.substring(index + 1, index + 3))
  }
}

/**
 * 一天中的分钟时间，格式如45:33
 */
case class HourMinute(val value: Short) {
  override def toString(): String = {
    var time = String.valueOf(value)
    if (value >= 6000) throw new RuntimeException("Invalid time " + time)
    while (time.length < 4) time = "0" + time
    time.substring(0, 2) + ":" + time.substring(2, 4)
  }

  def this(time: String) {
    this(HourMinute.convert(time))
  }

  def -(other: HourMinute): Short = {
    (this.value - other.value).asInstanceOf[Short]
  }
}
