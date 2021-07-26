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

import org.beangle.commons.lang.annotation.beta

import java.time.LocalDate

object WeekDay {

  def of(date: LocalDate): WeekDay = fromOrdinal(date.getDayOfWeek.getValue - 1)

  def of(id:Int):WeekDay = fromOrdinal(id - 1)
}

/**
  * 国家标准GBT 7408-2005
  */
@beta
enum WeekDay {
  case Mon,Tue,Wed,Thu,Fri,Sat,Sun

  def id :Int = ordinal + 1

  /**Java calendar Index
    */
  def index: Int =
    id match {
     case 7 => 1
     case _ => id + 1
    }

  def previous: WeekDay = {
    val preDayId = this.id - 1
    if (preDayId <= 0) WeekDay.Sun else WeekDay.of(preDayId)
  }

  def next: WeekDay = {
    val nextDayId = this.id + 1
    if (nextDayId > 7) WeekDay.Mon else WeekDay.of (this.id + 1)
  }
}
