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

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.time.{HourMinute, WeekDay, WeekTime}
import org.beangle.commons.lang.time.CycleTime.CycleTimeType.*
import org.beangle.commons.lang.time.CycleTime.{CycleTimeType, ToWeekTimeBuilder}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object CycleTime {
  enum CycleTimeType(val id: Int) {
    case Day extends CycleTimeType(1)
    case Week extends CycleTimeType(2)
    case Month extends CycleTimeType(4)
  }

  def apply(beginOn: LocalDate, endOn: LocalDate, beginAt: HourMinute, endAt: HourMinute,
            cycleCount: Int = 1, cycleType: CycleTimeType = CycleTimeType.Day): CycleTime = {
    val cd = new CycleTime
    cd.beginOn = beginOn
    cd.endOn = endOn
    cd.beginAt = beginAt
    cd.endAt = endAt
    cd.cycleCount = cycleCount
    cd.cycleType = cycleType
    cd
  }

  class ToWeekTimeBuilder(beginAt: HourMinute, endAt: HourMinute) {

    private val times = Collections.newBuffer[WeekTime]

    def build(): List[WeekTime] = times.toList

    /** 在TimeUnitBuilder里添加一个日期
     *
     * @param start
     */
    def add(start: LocalDate): Unit = {
      val time = WeekTime.of(start, beginAt, endAt)
      if (times.isEmpty) times += time
      else {
        times.find(t => t.mergeable(time, 15)) match {
          case Some(t) => t.merge(time, 15)
          case None => times += time
        }
      }
    }

    /** 添加以start为起点，cycle为单位，count为步进，循环添加日期，直到end为止
     *
     * @param start
     * @param end
     * @param cycleType
     * @param count
     */
    def addRange(start: LocalDate, end: LocalDate, cycleType: CycleTimeType, count: Int = 1): Unit = {
      var startOn = start
      while (!startOn.isAfter(end)) {
        add(startOn)
        cycleType match {
          case Day => startOn = startOn.plusDays(count)
          case Week => startOn = startOn.plusWeeks(count)
          case Month => startOn = startOn.plusMonths(count)
        }
      }
    }
  }
}

class CycleTime extends Cloneable with Serializable {
  /** 开始日期 */
  var beginOn: LocalDate = _
  /** 结束日期 */
  var endOn: LocalDate = _
  /** 开始时间 */
  var beginAt: HourMinute = _
  /** 结束时间 */
  var endAt: HourMinute = _
  /** 单位 */
  var cycleType: CycleTimeType = _
  /** 单位数量 */
  var cycleCount: Int = _

  def isOneDay: Boolean = {
    this.beginOn == this.endOn
  }

  def getCycleDays: Int = {
    cycleType match
      case Day => cycleCount
      case Month => cycleCount * 30
      case Week => cycleCount * 7
  }

  def convert(): List[WeekTime] = {
    val builder = new ToWeekTimeBuilder(beginAt, endAt)
    builder.addRange(beginOn, endOn, cycleType, cycleCount)
    builder.build()
  }

}
