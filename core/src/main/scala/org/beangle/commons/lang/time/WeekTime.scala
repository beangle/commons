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

import org.beangle.commons.lang.time.WeekDay._
import org.beangle.commons.lang.Objects
import java.{ util => ju }

/**循环时间*/
class WeekTime extends Ordered[WeekTime] with Serializable {

  var startOn: java.sql.Date = _

  /** 星期几 */
  var weekday: WeekDay = _

  /** 开始时间 */
  var beginAt: HourMinute = _

  /** 结束时间 */
  var endAt: HourMinute = _

  /** 周状态数字 */
  var weekstate: WeekState = _

  def this(other: WeekTime) {
    this()
    this.startOn = other.startOn
    this.weekday = other.weekday
    this.beginAt = other.beginAt
    this.endAt = other.endAt
    this.weekstate = other.weekstate
  }

  override def compare(other: WeekTime): Int = {
    Objects.compareBuilder.add(this.startOn, other.startOn).add(this.weekday, other.weekday)
      .add(this.beginAt, other.beginAt).add(this.endAt, other.endAt).add(this.weekstate, other.weekstate)
      .toComparison()
  }

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + startOn.hashCode()
    result = prime * result + (if ((weekstate == null)) 0 else weekstate.hashCode)
    result = prime * result + (if ((weekday == null)) 0 else weekday.hashCode)
    result = prime * result + (if ((beginAt == null)) 0 else beginAt.hashCode)
    result = prime * result + (if ((endAt == null)) 0 else endAt.hashCode)
    result
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case null => false
      case wt: WeekTime => {
        if (wt eq this) true
        else Objects.equalsBuilder.add(this.startOn, wt.startOn).add(this.weekday, wt.weekday).add(this.beginAt, wt.beginAt).add(this.endAt, wt.endAt).
          add(this.weekstate, wt.weekstate).isEquals
      }
      case _ => false
    }
  }

  def firstDate: ju.Date = {
    val cal = ju.Calendar.getInstance
    cal.setTime(startOn)
    while (cal.get(ju.Calendar.DAY_OF_WEEK) != weekday.index) {
      cal.add(ju.Calendar.DAY_OF_YEAR, 1)
    }
    cal.add(ju.Calendar.WEEK_OF_YEAR, weekstate.first - 1)
    cal.getTime
  }

//  def rebase(newStartOn: java.sql.Date): List[WeekTime] = {
//    if (newStartOn.after(startOn)) {
//      if (newStartOn.after(firstDate)) {
//        throw new RuntimeException("Cannot rebase a date after firstDate!")
//      } else {
//        val nwt = new WeekTime(this)
//        nwt.startOn = newStartOn
//        List(nwt)
//      }
//    } else {
//      val newStartCal = ju.Calendar.getInstance
//      newStartCal.setTime(newStartOn)
//      var days = 0
//      while (newStartCal.before(startOn)) {
//        newStartCal.add(ju.Calendar.DAY_OF_YEAR, 1)
//        days += 1
//      }
//      val weekcnt = days / 7
//      var newWeeks = this.toString();
//      newWeeks += ("0" * weekcnt)
//      if (newWeeks.length <= 64) {
//        val nwt = new WeekTime(this)
//        nwt.startOn = newStartOn
//        nwt.weekstate = new WeekState(newWeeks)
//        List(nwt)
//      } else {
//        val nwt1 = new WeekTime(this)
//        nwt1.startOn = newStartOn
//        nwt1.weekstate = new WeekState(newWeeks.substring(0, 64))
//        val nwt2 = new WeekTime(this)
//        newStartCal.add(ju.Calendar.WEEK_OF_YEAR, 64)
//        nwt2.startOn = new java.sql.Date(newStartCal.getTime.getTime)
//        nwt2.weekstate = new WeekState(newWeeks.substring(64))
//        List(nwt1, nwt2)
//      }
//    }
//  }
}

