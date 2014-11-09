package org.beangle.commons.lang.time

import org.beangle.commons.lang.Strings

object WeekState {
  def apply(value: String): WeekState = {
    new WeekState(java.lang.Long.parseLong(value, 2))
  }
}
class WeekState(val value: Long) {

  override def toString: String = {
    java.lang.Long.toBinaryString(value)
  }

  def span: Tuple2[Int, Int] = {
    val str = toString
    val length = str.length - 1
    new Tuple2(length - str.lastIndexOf('1'), length - str.indexOf('1'))
  }

  def weeks: Int = {
    Strings.count(toString, "1")
  }

  def weekList: List[Int] = {
    val weekstr = toString
    var i = weekstr.length - 1
    val result = new collection.mutable.ListBuffer[Int]
    while (i >= 0) {
      if (weekstr.charAt(i) == '1') result += (weekstr.length - 1 - i)
    }
    result.toList
  }

  def isOccupied(week: Int): Boolean = {
    (value & (1l << week)) > 0
  }

}