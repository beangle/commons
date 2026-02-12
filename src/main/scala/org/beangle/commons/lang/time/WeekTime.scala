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

import org.beangle.commons.bean.component
import org.beangle.commons.lang.Objects

import java.time.{LocalDate, LocalDateTime}
import scala.collection.mutable

/** Recurring weekly time slot. */
@component
class WeekTime extends Ordered[WeekTime] with Serializable {

  /** Start date of the year/semester. */
  var startOn: LocalDate = _

  /** Start time of day. */
  var beginAt: HourMinute = _

  /** End time of day. */
  var endAt: HourMinute = _

  /** Week state bitmap (which weeks). */
  var weekstate: WeekState = _

  /** Copy constructor. */
  def this(other: WeekTime) = {
    this()
    this.startOn = other.startOn
    this.beginAt = other.beginAt
    this.endAt = other.endAt
    this.weekstate = other.weekstate
  }

  /** Dates covered by this WeekTime (startOn + each week). */
  def dates: List[LocalDate] = weekstate.weeks.map { x => startOn.plusWeeks(x - 1) }

  /** First date in the range. */
  def firstDay: LocalDate = startOn.plusWeeks(weekstate.first - 1)

  /** First day at beginAt time. */
  def firstTime: LocalDateTime = firstDay.atTime(beginAt.toLocalTime)

  /** Last date in the range. */
  def lastDay: LocalDate = startOn.plusWeeks(weekstate.last - 1)

  /** Weekday of startOn. */
  def weekday: WeekDay = WeekDay.of(startOn)

  /** Returns true if this overlaps with o (same startOn, overlapping weeks and time). */
  def isOverlap(o: WeekTime): Boolean = {
    startOn.equals(o.startOn) && weekstate.isOverlap(o.weekstate) & beginAt < o.endAt & o.beginAt < endAt
  }

  override def compare(other: WeekTime): Int = {
    Objects.compareBuilder.add(this.startOn, other.startOn)
      .add(this.beginAt, other.beginAt).add(this.endAt, other.endAt).add(this.weekstate, other.weekstate)
      .toComparison
  }

  override def toString: String = s"[startOn:$startOn, beginAt:$beginAt endAt:$endAt weekstate:$weekstate]"

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + startOn.hashCode()
    result = prime * result + (if (weekstate == null) 0 else weekstate.hashCode)
    result = prime * result + (if (beginAt == null) 0 else beginAt.hashCode)
    result = prime * result + (if (endAt == null) 0 else endAt.hashCode)
    result
  }

  override def equals(obj: Any): Boolean =
    obj match {
      case null => false
      case wt: WeekTime =>
        if (wt eq this) true
        else Objects.equalsBuilder.add(this.startOn, wt.startOn).add(this.beginAt, wt.beginAt).add(this.endAt, wt.endAt).
          add(this.weekstate, wt.weekstate).isEquals
      case _ => false
    }

  /** Attempts to merge this with another WeekTime if mergeable.
   *
   * @param w2     the other WeekTime
   * @param minGap minimum gap (minutes) to consider adjacent
   * @return true if merged
   */
  def merge(w2: WeekTime, minGap: Int): Boolean =
    if (mergeable(w2, minGap)) {
      doMerge(w2)
      true
    } else
      false

  /** Whether this and w2 can be merged. Requires same weekState and weekday, and either adjacent
   * or overlapping time; or same time to merge week states.
   *
   * @param w2     the other WeekTime
   * @param minGap minimum gap to treat as adjacent
   * @return true if mergeable
   */
  def mergeable(w2: WeekTime, minGap: Int): Boolean =
    if (this.startOn == w2.startOn)
      if (this.weekstate == w2.weekstate)
        if (this.beginAt.interval(w2.endAt) < minGap || (w2.beginAt.interval(this.endAt) < minGap))
          true
        else
          (this.beginAt.value <= w2.endAt.value) && (w2.beginAt.value <= this.endAt.value)
      else
        this.beginAt == w2.beginAt && this.endAt == w2.endAt
    else
      false

  /** Merges w2 into this. Call only when mergeable. */
  private def doMerge(w2: WeekTime): Unit =
    if (this.weekstate == w2.weekstate) {
      if (w2.beginAt.value < this.beginAt.value)
        this.beginAt = w2.beginAt
      if (w2.endAt.value > this.endAt.value)
        this.endAt = w2.endAt
    } else
      this.weekstate = this.weekstate | w2.weekstate
}

/** WeekTime factory. */
object WeekTime {

  /** Builds WeekTime for a date with time range (beginAt and endAt on same day).
   *
   * @param startOn the start date
   * @param beginAt the start time
   * @param endAt   the end time
   * @return the WeekTime
   */
  def of(startOn: LocalDate, beginAt: HourMinute, endAt: HourMinute): WeekTime = {
    val time = of(startOn)
    time.beginAt = beginAt
    time.endAt = endAt
    time
  }

  /** Builds WeekTime for a date (year start + week bitmap from date).
   *
   * @param ld the date
   * @return WeekTime with startOn and weekstate for that week
   */
  def of(ld: LocalDate): WeekTime = {
    val yearStartOn = getStartOn(ld.getYear, WeekDay.of(ld))
    val weektime = new WeekTime
    weektime.startOn = yearStartOn
    weektime.weekstate = WeekState.of(Weeks.between(yearStartOn, ld) + 1)
    weektime
  }

  /** Returns the first occurrence of the given weekday in the year.
   *
   * @param year    the year
   * @param weekday the weekday
   * @return the date
   */
  def getStartOn(year: Int, weekday: WeekDay): LocalDate = {
    var startDate = LocalDate.of(year, 1, 1)
    while (startDate.getDayOfWeek.getValue != weekday.id)
      startDate = startDate.plusDays(1)
    startDate
  }

  /** Creates Builder for semester/year range (startOn to first weekend).
   *
   * @param startOn  semester start date
   * @param firstDay first day of week
   * @return Builder
   */
  def newBuilder(startOn: LocalDate, firstDay: WeekDay): Builder = {
    var endOn = startOn
    val weekendDay = firstDay.previous
    while (endOn.getDayOfWeek.getValue != weekendDay.id)
      endOn = endOn.plusDays(1)
    new Builder(startOn, endOn)
  }

  /** Builds WeekTime instances for given weekday and week indices. */
  class Builder(startOn: LocalDate, firstWeekEndOn: LocalDate) {

    /** Builds WeekTime for weekday and week index collection.
     *
     * @param weekday the weekday
     * @param weeks   week indices (1-based)
     * @return Seq of WeekTime
     */
    def build(weekday: WeekDay, weeks: Iterable[Int]): Seq[WeekTime] =
      build(weekday, weeks.toArray)

    /** Builds WeekTime for weekday and week index array.
     *
     * @param weekday the weekday
     * @param weeks   week indices (1-based)
     * @return Seq of WeekTime
     */
    def build(weekday: WeekDay, weeks: Array[Int]): Seq[WeekTime] = {
      val times = new mutable.HashMap[Int, WeekTime]
      var startDate = startOn
      while (startDate.getDayOfWeek.getValue != weekday.id)
        startDate = startDate.plusDays(1)
      var minWeek = 1
      if (startDate.isAfter(firstWeekEndOn)) minWeek = 2

      for (week <- weeks; if week >= minWeek) {
        val oneday = startDate.plusWeeks(week - 1)
        val year = oneday.getYear
        val yearStartOn = WeekTime.getStartOn(year, weekday)
        val weektime =
          times.get(year) match {
            case None =>
              val wt = new WeekTime
              times.put(year, wt)
              wt.startOn = yearStartOn
              wt.weekstate = new WeekState(0)
              wt
            case Some(wt) => wt
          }
        weektime.weekstate = weektime.weekstate | WeekState.of(Weeks.between(yearStartOn, oneday) + 1)
      }
      times.values.toSeq
    }
  }
}
