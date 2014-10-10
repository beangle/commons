package org.beangle.commons.lang.time

/**
 * 国家标准GBT 7408-2005
 */
object WeekDays extends Enumeration(1) {
  class WeekDay extends super.Val
  val Mon, Tue, Wed, Thu, Fri, Sat, Sun = WeekDayValue

  private def WeekDayValue(): WeekDay = {
    new WeekDay()
  }
}