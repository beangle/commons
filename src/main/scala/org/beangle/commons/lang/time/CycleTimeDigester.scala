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
import org.beangle.commons.lang.time.CycleTime.CycleTimeType

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.collection.mutable

/** Converts WeekTime list to human-readable cycle string (e.g. "2024-01-01~01-31 每周三 09:00~11:00"). */
object CycleTimeDigest {
  private val wMap = Map("Mon" -> "一", "Tue" -> "二", "Wed" -> "三", "Thu" -> "四", "Fri" -> "五", "Sat" -> "六", "Sun" -> "日")
  private val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private val format2 = DateTimeFormatter.ofPattern("MM-dd")

  /** Digests WeekTime list into compact Chinese cycle description.
   *
   * @param times     the WeekTime list
   * @param delimeter separator between cycle entries (default: ",")
   * @return formatted string
   */
  def digest(times: collection.Seq[WeekTime], delimeter: String = ","): String = {
    if (times.isEmpty) return ""
    val timeList = Collections.newBuffer[String]
    val timeMap = new mutable.HashMap[(HourMinute, HourMinute), mutable.HashSet[LocalDate]]()
    times foreach { t =>
      val dates = timeMap.getOrElseUpdate((t.beginAt, t.endAt), new mutable.HashSet[LocalDate]())
      dates.addAll(t.dates)
    }
    val cycleTimes = new mutable.ArrayBuffer[CycleTime]
    timeMap foreach { case ((b, e), dates) =>
      if (dates.size == 1) {
        cycleTimes.addOne(CycleTime(dates.head, dates.head, b, e, 1, CycleTimeType.Day))
      } else {
        val dateList = new mutable.ArrayBuffer[LocalDate]
        dateList.addAll(dates).sortInPlace()

        val intervalMap = calcIntervals(dateList)
        if (intervalMap.size == 1) { // single interval
          val period = intervalMap.head._1
          if (period % 7 == 0) {
            cycleTimes.addOne(CycleTime(dateList.head, dateList.last, b, e, period / 7, CycleTimeType.Week))
          } else {
            cycleTimes.addOne(CycleTime(dateList.head, dateList.last, b, e, period, CycleTimeType.Day))
          }
        } else { // multiple intervals
          dateList.groupBy(_.getDayOfWeek) foreach { case (dofw, wdates) =>
            val wIntervalMap = calcIntervals(wdates)
            if (wIntervalMap.size == 1) {
              val period = wIntervalMap.head._1
              cycleTimes.addOne(CycleTime(wdates.head, wdates.last, b, e, period / 7, CycleTimeType.Week))
            } else {
              wdates foreach { d => cycleTimes.addOne(CycleTime(d, d, b, e)) }
            }
          }
        }
      }
    }

    cycleTimes.sortBy(x => x.beginOn.toString + "" + x.beginAt.toString).foreach(cd => {
      val sb = new StringBuilder
      if (cd.isOneDay) sb.append(cd.endOn.format(format))
      else {
        sb.append(cd.beginOn.format(format))
        sb.append("~")
        if (cd.beginOn.getYear == cd.endOn.getYear) sb.append("").append(cd.endOn.format(format2))
        else sb.append("").append(cd.endOn.format(format))
        if (cd.cycleType == CycleTimeType.Week) {
          if (cd.cycleCount != 1) sb.append(" 每" + cd.cycleCount + "周周") else sb.append(" 每周")

          val wd = WeekDay.of(cd.beginOn)
          sb.append(wMap(wd.toString))
        } else {
          if (cd.cycleCount == 1) {
            if (cd.beginOn != cd.endOn) sb.append(" 每天")
          } else {
            sb.append(" 每" + cd.cycleCount + "天")
          }
        }
      }
      sb.append(" ")
      sb.append(cd.beginAt)
      sb.append("~")
      sb.append(cd.endAt)
      timeList.+=(sb.toString)
    })
    Strings.join(timeList, delimeter)
  }

  private def calcIntervals(dates: collection.Seq[LocalDate]): Map[Int, Int] = {
    val intervalMap = new mutable.HashMap[Int, Int]()
    var head: LocalDate = null
    dates.foreach { d =>
      if (null != head) {
        val i = Math.abs(ChronoUnit.DAYS.between(d, head).intValue)
        intervalMap.put(i, intervalMap.getOrElseUpdate(i, 0) + 1)
      }
      head = d
    }
    intervalMap.toMap
  }
}
