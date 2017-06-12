/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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

import java.{ util => ju }

import org.beangle.commons.bean.component
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.annotation.beta
import org.beangle.commons.lang.time.WeekDay.WeekDay
import java.util.Calendar
import java.time.LocalDate

/**循环时间*/
@beta
@component
class WeekTime extends Ordered[WeekTime] with Serializable {

  /**起始日期*/
  var startOn: java.sql.Date = _

  /** 开始时间 */
  var beginAt: HourMinute = _

  /** 结束时间 */
  var endAt: HourMinute = _

  /** 周状态数字 */
  var weekstate: WeekState = _

  def this(other: WeekTime) {
    this()
    this.startOn = other.startOn
    this.beginAt = other.beginAt
    this.endAt = other.endAt
    this.weekstate = other.weekstate
  }

  def dates: List[java.sql.Date] = {
    val s = startOn.toLocalDate()
    weekstate.weeks.map { x => java.sql.Date.valueOf(s.plusWeeks(x - 1)) }
  }

  def firstDay: java.sql.Date = {
    val cal = ju.Calendar.getInstance
    cal.setTime(startOn)
    cal.add(ju.Calendar.WEEK_OF_YEAR, weekstate.first - 1)
    new java.sql.Date(cal.getTime.getTime)
  }

  def lastDay: java.sql.Date = {
    val cal = ju.Calendar.getInstance
    cal.setTime(startOn)
    cal.add(ju.Calendar.WEEK_OF_YEAR, weekstate.last - 1)
    new java.sql.Date(cal.getTime.getTime)
  }

  def weekday: WeekDay = {
    WeekDay.of(startOn)
  }

  def isOverlap(o: WeekTime): Boolean = {
    startOn.equals(o.startOn) && weekstate.isOverlap(o.weekstate) & beginAt < o.endAt & o.beginAt < endAt;
  }

  override def compare(other: WeekTime): Int = {
    Objects.compareBuilder.add(this.startOn, other.startOn)
      .add(this.beginAt, other.beginAt).add(this.endAt, other.endAt).add(this.weekstate, other.weekstate)
      .toComparison()
  }

  override def toString: String = {
    s"[startOn:$startOn, beginAt:$beginAt endAt:$endAt weekstate:$weekstate]"
  }

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + startOn.hashCode()
    result = prime * result + (if ((weekstate == null)) 0 else weekstate.hashCode)
    result = prime * result + (if ((beginAt == null)) 0 else beginAt.hashCode)
    result = prime * result + (if ((endAt == null)) 0 else endAt.hashCode)
    result
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case null => false
      case wt: WeekTime => {
        if (wt eq this) true
        else Objects.equalsBuilder.add(this.startOn, wt.startOn).add(this.beginAt, wt.beginAt).add(this.endAt, wt.endAt).
          add(this.weekstate, wt.weekstate).isEquals
      }
      case _ => false
    }
  }
}
