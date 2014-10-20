package org.beangle.commons.conversion.converter

import org.beangle.commons.lang.time.HourMinute
import org.beangle.commons.lang.Strings
import org.beangle.commons.conversion.Converter

object String2HourMinuteConverter extends Converter[String, HourMinute] {

  override def apply(input: String): HourMinute = {
    if (Strings.isEmpty(input)) null else HourMinute.apply(input)
  }
}