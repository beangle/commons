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
import org.beangle.commons.lang.annotation.beta
import org.beangle.commons.lang.time.WeekDay

import java.time.LocalDate
import scala.collection.mutable

/** 循环时间 */
@beta
@component
class WeekTime extends Ordered[WeekTime] with Serializable {

  /** 起始日期 */
  var startOn: LocalDate = _

  /** 开始时间 */
  var beginAt: HourMinute = _

  /** 结束时间 */
  var endAt: HourMinute = _

  /** 周状态数字 */
  var weekstate: WeekState = _

  def this(other: WeekTime) = {
    this()
    this.startOn = other.startOn
    this.beginAt = other.beginAt
    this.endAt = other.endAt
    this.weekstate = other.weekstate
  }

  def dates: List[LocalDate] =
    weekstate.weeks.map { x => startOn.plusWeeks(x - 1) }

  def firstDay: LocalDate =
    startOn.plusWeeks(weekstate.first - 1)

  def lastDay: LocalDate =
    startOn.plusWeeks(weekstate.last - 1)

  def weekday: WeekDay =
    WeekDay.of(startOn)

  def isOverlap(o: WeekTime): Boolean =
    startOn.equals(o.startOn) && weekstate.isOverlap(o.weekstate) & beginAt < o.endAt & o.beginAt < endAt

  override def compare(other: WeekTime): Int =
    Objects.compareBuilder.add(this.startOn, other.startOn)
      .add(this.beginAt, other.beginAt).add(this.endAt, other.endAt).add(this.weekstate, other.weekstate)
      .toComparison()

  override def toString: String =
    s"[startOn:$startOn, beginAt:$beginAt endAt:$endAt weekstate:$weekstate]"

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

  /** 尝试合并两个时间
    *
    * @param w2 second weektime
    * @return true if merged
    */
  def merge(w2: WeekTime, minGap: Int): Boolean =
    if (mergeable(w2, minGap)) {
      doMerge(w2)
      true
    } else
      false

  /** 判断合并两个时间是否可以
    * 标准为 （weekState、weekday相等） 且 （相连时间 或 时间相交）
    * 或者时间相等则可以合并周次
    *
    * @param w2 second weektime
    * @return true if merged
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

  /** 将两时间进行合并，前提是这两时间可以合并
    *
    * @param w2 weektime
    */
  private def doMerge(w2: WeekTime): Unit =
    if (this.weekstate == w2.weekstate) {
      if (w2.beginAt.value < this.beginAt.value)
        this.beginAt = w2.beginAt
      if (w2.endAt.value > this.endAt.value)
        this.endAt = w2.endAt
    } else
      this.weekstate = this.weekstate | w2.weekstate
}

object WeekTime {

  /** 构造某个日期（beginAt, endAt必须是同一天，只是时间不同）的WeekTime
    *
    * @param beginAt beginAt
    * @param endAt   endAt
    * @return
    */
  def of(startOn: LocalDate, beginAt: HourMinute, endAt: HourMinute): WeekTime = {
    val time = of(startOn)
    time.beginAt = beginAt
    time.endAt = endAt
    time
  }

  /**
    *
    * @param ld date
    * @return
    */
  def of(ld: LocalDate): WeekTime = {
    val yearStartOn = getStartOn(ld.getYear, WeekDay.of(ld))
    val weektime = new WeekTime
    weektime.startOn = yearStartOn
    weektime.weekstate = WeekState.of(Weeks.between(yearStartOn, ld) + 1)
    weektime
  }

  /** 查询该年份第一个指定day的日期
    *
    * @param year    year
    * @param weekday weekday
    * @return 指定day的第一天
    */
  def getStartOn(year: Int, weekday: WeekDay): LocalDate = {
    var startDate = LocalDate.of(year, 1, 1)
    while (startDate.getDayOfWeek.getValue != weekday.id)
      startDate = startDate.plusDays(1)
    startDate
  }

  def newBuilder(startOn: LocalDate, firstDay: WeekDay): Builder = {
    var endOn = startOn
    val weekendDay = firstDay.previous
    while (endOn.getDayOfWeek.getValue != weekendDay.id)
      endOn = endOn.plusDays(1)
    new Builder(startOn, endOn)
  }

  class Builder(startOn: LocalDate, firstWeekEndOn: LocalDate) {

    def build(weekday: WeekDay, weeks: Iterable[Int]): Seq[WeekTime] =
      build(weekday, weeks.toArray)

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
