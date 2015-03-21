package org.beangle.commons.lang.time

import java.{ util => ju }
/**
 * 国家标准GBT 7408-2005
 */
object WeekDays extends Enumeration(1) {
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

  //FIXME need test
  def of(date: ju.Date): WeekDay = {
    val cal = ju.Calendar.getInstance
    cal.setTime(date)
    WeekDays(index2Id(cal.get(ju.Calendar.DAY_OF_WEEK))).asInstanceOf[WeekDay]
  }
}