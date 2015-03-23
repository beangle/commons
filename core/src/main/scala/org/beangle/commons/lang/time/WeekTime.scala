/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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

import WeekDays._
import org.beangle.commons.lang.Objects

/**循环时间*/
class WeekTime extends Ordered[WeekTime] with Serializable {

  /** 星期几 */
  var day: WeekDay = _

  /** 开始时间 */
  var begin: HourMinute = _

  /** 结束时间 */
  var end: HourMinute = _

  /** 周状态数字 */
  var state: WeekState = _

  override def compare(other: WeekTime): Int = {
    Objects.compareBuilder.add(this.day, other.day)
      .add(this.begin, other.begin).add(this.end, other.end)
      .toComparison()
  }
  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + (if ((state == null)) 0 else state.hashCode)
    result = prime * result + (if ((day == null)) 0 else day.hashCode)
    result = prime * result + (if ((begin == null)) 0 else begin.hashCode)
    result = prime * result + (if ((end == null)) 0 else end.hashCode)
    result
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case null => false
      case wt: WeekTime => {
        if (wt eq this) true
        else Objects.equalsBuilder.add(this.day, wt.day).add(this.begin, wt.begin).add(this.end, wt.end).
          add(this.state, wt.state).isEquals
      }
      case _ => false
    }
  }
}

class YearWeekTime extends WeekTime {
  var year: Int = _

  override def compare(wt: WeekTime): Int = {
    val other = wt.asInstanceOf[YearWeekTime]
    Objects.compareBuilder.add(this.year, other.year).add(this.day, other.day)
      .add(this.begin, other.begin).add(this.end, other.end)
      .toComparison()
  }

  def this(other: YearWeekTime) {
    this()
    this.year = other.year
    this.day = other.day
    this.begin = other.begin
    this.end = other.end
    this.state = other.state
  }

  override def clone(): YearWeekTime = {
    new YearWeekTime(this)
  }

  override def hashCode(): Int = {
    var result = super.hashCode()
    val prime = 31
    result = prime * result + year
    result
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case null => false
      case wt: YearWeekTime => {
        if (wt eq this) true
        else Objects.equalsBuilder.add(this.day, wt.day).add(this.begin, wt.begin).add(this.end, wt.end).
          add(this.state, wt.state).add(this.year, wt.year).isEquals
      }
      case _ => false
    }
  }
}