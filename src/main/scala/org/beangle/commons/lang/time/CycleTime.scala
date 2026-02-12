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
import org.beangle.commons.lang.time.CycleTime.CycleTimeType.*
import org.beangle.commons.lang.time.CycleTime.{CycleTimeType, ToWeekTimeBuilder}

import java.time.LocalDate
import scala.collection.mutable

/** CycleTime factory and CycleTimeType. */
object CycleTime {

  enum CycleTimeType(val id: Int) {
    case Day extends CycleTimeType(1)
    case Week extends CycleTimeType(2)
    case Month extends CycleTimeType(4)
  }

  /** Creates a CycleTime with the given date range, time slot, and cycle settings.
   *
   * @param beginOn    start date
   * @param endOn      end date
   * @param beginAt    start time
   * @param endAt      end time
   * @param cycleCount number of units per cycle
   * @param cycleType  the cycle unit (Day, Week, Month)
   * @return configured CycleTime
   */
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

  /** Builds List[WeekTime] from date ranges with time slot, merging adjacent entries. */
  class ToWeekTimeBuilder(beginAt: HourMinute, endAt: HourMinute) {

    private val times = Collections.newBuffer[WeekTime]

    /** Returns built list of WeekTime. */
    def build(): List[WeekTime] = times.toList

    /** Adds a date to the builder, merging if adjacent to existing.
     *
     * @param start the date to add
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

    /** Adds dates from start to end, stepping by count units of cycleType.
     *
     * @param start     the start date
     * @param end       the end date
     * @param cycleType the cycle unit (Day, Week, Month)
     * @param count     the step size
     */
    def addRange(start: LocalDate, end: LocalDate, cycleType: CycleTimeType, count: Int = 1): Unit = {
      var startOn = start
      require(count > 0, "count should be greater than 0")
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

/** Recurring time slot with begin/end dates, times, and cycle unit (Day/Week/Month). */
class CycleTime extends Cloneable with Serializable {

  /** Start date. */
  var beginOn: LocalDate = _
  /** End date. */
  var endOn: LocalDate = _
  /** Start time. */
  var beginAt: HourMinute = _
  /** End time. */
  var endAt: HourMinute = _
  /** Cycle unit (Day, Week, Month). */
  var cycleType: CycleTimeType = _
  /** Number of units per cycle. */
  var cycleCount: Int = _

  /** Returns true if beginOn == endOn. */
  def isOneDay: Boolean = {

    this.beginOn == this.endOn
  }

  /** Returns total days in cycle (Day=count, Week=7*count, Month=30*count). */
  def getCycleDays: Int = {

    cycleType match
      case Day => cycleCount
      case Month => cycleCount * 30
      case Week => cycleCount * 7
  }

  /** Converts cycle to List[WeekTime] via ToWeekTimeBuilder. */
  def convert(): List[WeekTime] = {

    val builder = new ToWeekTimeBuilder(beginAt, endAt)
    builder.addRange(beginOn, endOn, cycleType, cycleCount)
    builder.build()
  }

}
