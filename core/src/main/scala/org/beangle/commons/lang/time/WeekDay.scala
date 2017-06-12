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
import org.beangle.commons.lang.annotation.beta
/**
 * 国家标准GBT 7408-2005
  */
@beta
object WeekDay extends Enumeration(1) {
  class WeekDay extends super.Val {
    /**
     * Java calendar Index
     */
    def index: Int = {
      id match {
        case 7 => 1
        case _ => id + 1
      }
    }
  }

  private def index2Id(idx: Int): Int = {
    idx match {
      case 1 => 7
      case _ => idx - 1
    }
  }

  val Mon, Tue, Wed, Thu, Fri, Sat, Sun = WeekDayValue

  private def WeekDayValue(): WeekDay = {
    new WeekDay()
  }

  def of(date: ju.Date): WeekDay = {
    val cal = ju.Calendar.getInstance
    cal.setTime(date)
    WeekDay(index2Id(cal.get(ju.Calendar.DAY_OF_WEEK))).asInstanceOf[WeekDay]
  }
}
