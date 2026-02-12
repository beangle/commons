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

package org.beangle.commons.lang.time

import org.beangle.commons.lang.Numbers.toShort
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.value

/** Hour:minute of day (e.g. 09:30), value = hour*100+minute.
 *
 * @version 4.0.5
 * @since 4.0.5
 */
object HourMinute {

  /** Zero hour-minute (00:00). */
  val Zero = new HourMinute(0)

  /** Parses a time string in HH:mm format to HourMinute. Returns Zero for blank input.
   *
   * @param time time string (e.g. "09:30")
   * @return HourMinute instance
   */
  def apply(time: String): HourMinute = {
    if Strings.isBlank(time) then Zero else new HourMinute(convert(time))
  }

  /** Creates HourMinute from hour and minute components.
   *
   * @param hour   hour (0–23)
   * @param minute minute (0–59)
   * @return HourMinute instance
   */
  def of(hour: Int, minute: Int): HourMinute = new HourMinute((hour * 100 + minute).asInstanceOf[Short])

  /** Creates HourMinute from a LocalTime.
   *
   * @param time LocalTime to convert
   * @return HourMinute instance
   */
  def of(time: java.time.LocalTime): HourMinute = of(time.getHour, time.getMinute)

  /** Converts a time string (HH:mm) to internal Short value (HHmm).
   *
   * @param time time string (e.g. "09:30")
   * @return value as Short
   */
  def convert(time: String): Short = {
    val index = time.indexOf(':')
    require(index > 0 && time.length <= 5, "illegal time,it should with 00:00 format")
    require(
      (toShort(time.substring(0, index)) < 60 && toShort(time.substring(index + 1, index + 3)) < 60),
      s"illegal time $time,it should within 60:60.")
    toShort(time.substring(0, index) + time.substring(index + 1, index + 3))
  }
}

/** Time of day in minutes (format HHmm, e.g. 2333 for 23:33). */
@value
class HourMinute(val value: Short) extends Serializable with Ordered[HourMinute] {

  require(value <= 2400, s"Invalid time value $value,It should less than or equals 2400")

  /** Converts to java.time.LocalTime. */
  def toLocalTime: java.time.LocalTime = java.time.LocalTime.of(hour, minute)

  override def toString: String = {
    var time = String.valueOf(value)
    while (time.length < 4) time = "0" + time
    time.substring(0, 2) + ":" + time.substring(2, 4)
  }

  override def compare(o: HourMinute): Int = this.value - o.value

  /** Hour component (0–23). */
  def hour: Int = value / 100

  /** Minute component (0–59). */
  def minute: Int = value % 100

  /** Returns the absolute difference in minutes between this and another HourMinute.
   *
   * @param other other HourMinute
   * @return minutes between the two times
   */
  def interval(other: HourMinute): Int = Math.abs(this.minutes - other.minutes)

  /** Adds a duration in minutes. Wraps around midnight for overflow/underflow.
   *
   * @param minutesDuration minutes to add (can be negative)
   * @return new HourMinute
   */
  def +(minutesDuration: Int): HourMinute = {
    var minutesValue = minutes + minutesDuration
    val day = 24 * 60
    if (minutesValue < 0)
      while (minutesValue < 0)
        minutesValue += day
    else
      while (minutesValue >= day)
        minutesValue -= day
    new HourMinute(((minutesValue / 60) * 100 + minutesValue % 60).asInstanceOf[Short])
  }

  /** Subtracts a duration in minutes. Wraps around midnight for underflow.
   *
   * @param minutesDuration minutes to subtract
   * @return new HourMinute
   */
  def -(minutesDuration: Int): HourMinute = this + (0 - minutesDuration)

  private def minutes: Int = hour * 60 + minute

  /** Returns the minute difference between this and another HourMinute.
   *
   * @param other other HourMinute
   * @return minutes (can be negative)
   */
  def -(other: HourMinute): Short = (this.minutes - other.minutes).asInstanceOf[Short]

  override def equals(obj: Any): Boolean = {
    obj match {
      case hm: HourMinute => hm.value == this.value
      case _ => false
    }
  }

  override def hashCode: Int = value
}
