package org.beangle.commons.lang.time

/**
 * 参考国家标准GBT 7408-2005
 */
object WeekDay extends Enumeration(1) {
  type WeekDay = Value
  val Mon = Value("Mon")
  val Tue = Value("Tue")
  val Wed = Value("Wed")
  val Thu = Value("Thu")
  val Fri = Value("Fri")
  val Sat = Value("Sat")
  val Sun = Value("Sun")
}
