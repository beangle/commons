package org.beangle.commons.lang.time

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
  val Mon, Tue, Wed, Thu, Fri, Sat, Sun = WeekDayValue

  private def WeekDayValue(): WeekDay = {
    new WeekDay()
  }

}