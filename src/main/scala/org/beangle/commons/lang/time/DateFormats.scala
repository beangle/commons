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

import java.text.*
import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util as ju
import java.util.regex.Pattern
import java.util.{Calendar, GregorianCalendar, SimpleTimeZone, TimeZone}

object DateFormats {

  /** Preferred HTTP date format (RFC 1123).
   *
   * @see https://www.ietf.org/rfc/rfc1123.txt
   */
  object Http {
    private val format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
    format.setTimeZone(TimeZone.getTimeZone("GMT"))

    def format(date: ju.Date): String = format.format(date)

    def format(date: java.time.ZonedDateTime): String = {
      date.format(DateTimeFormatter.RFC_1123_DATE_TIME)
    }

    def format(date: java.time.LocalDateTime): String = {
      date.atZone(ZoneId.of("GMT")).format(DateTimeFormatter.RFC_1123_DATE_TIME)
    }

    def parse(date: String): ZonedDateTime = {
      ZonedDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME)
    }
  }

  val RFC1123 = Http

  /** This object handles Internet date/time strings in accordance with RFC 3339. It
   * provides methods to convert from Calendar to RFC 3339 format strings and to parse these strings back into
   * the same constructs.
   * Strings are parsed in accordance with the RFC 3339 format:
   *
   * <pre>
   * YYYY-MM-DD(T|t|\s)hh:mm:ss[.ddd][tzd]
   * </pre>
   *
   * The <code>tzd</code> represents the time zone designator and is either an
   * upper or lower case 'Z' indicating UTC or a signed <code>hh:mm</code> offset.
   *
   * https://www.ietf.org/rfc/rfc3339.txt
   *
   * @see www.hackcraft.net/web/datetime
   */
  private object Internet {

    val df2 = new DecimalFormat("00")
    val df3 = new DecimalFormat("000")
    val df4 = new DecimalFormat("0000")

    /** The Regex pattern to match. */
    val pattern = buildPattern()

    private def buildPattern(): Pattern = {
      val reDate = "(\\d{4})-(\\d{2})-(\\d{2})"
      val reTime = "(\\d{2}):(\\d{2}):(\\d{2})(\\.\\d+)?"
      val reZone = "(?:([zZ])|(?:(\\+|\\-)(\\d{2}):(\\d{2})))"
      val re = reDate + "[tT\\s]" + reTime + reZone;
      Pattern.compile(re)
    }

    /**
     * Our private parse utility that parses the string, clears the calendar,
     * and then sets the fields.
     *
     * @param s   the string to parse
     * @param cal the calendar object to populate
     * @throws IllegalArgumentException
     * if the string is not a valid RFC 3339 date/time string
     */
    private def parse(s: String, cal: Calendar): Unit = {
      val m = pattern.matcher(s)
      if (!m.matches())
        throw new IllegalArgumentException("Invalid date/time: " + s);
      cal.clear()
      cal.set(Calendar.YEAR, Integer.parseInt(m.group(1)))
      cal.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1)
      cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(m.group(3)))
      cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(4)))
      cal.set(Calendar.MINUTE, Integer.parseInt(m.group(5)))
      cal.set(Calendar.SECOND, Integer.parseInt(m.group(6)))
      if (m.group(7) != null) {
        val fraction = java.lang.Float.parseFloat(m.group(7))
        cal.set(Calendar.MILLISECOND, (fraction * 1000F).asInstanceOf[Int])
      }
      if (m.group(8) != null)
        cal.setTimeZone(new SimpleTimeZone(0, "Z"))
      else {
        val sign = if (m.group(9).equals("-")) -1 else 1
        val tzhour = Integer.parseInt(m.group(10))
        val tzminute = Integer.parseInt(m.group(11))
        val offset = sign * ((tzhour * 60) + tzminute)
        val id = Integer.toString(offset)
        cal.setTimeZone(new SimpleTimeZone(offset * 60000, id));
      }
    }

    /**
     * Parses an RFC 3339 date/time string to a Calendar object.
     *
     * @param s the string to parse
     * @return the Calendar object
     * @throws IllegalArgumentException
     * if the string is not a valid RFC 3339 date/time string
     */
    def parse(s: String): Calendar = {
      val cal = new GregorianCalendar()
      parse(s, cal);
      cal
    }

    /**
     * Converts the specified Calendar object to an RFC 3339 date/time string.
     * Unlike the toString methods for Date and long, no additional variant of
     * this method taking a time zone is provided since the time zone is built
     * into the Calendar object.
     *
     * @param cal the Calendar object
     * @return an RFC 3339 date/time string (does not include milliseconds)
     */
    def format(cal: Calendar): String = {
      val buf = new StringBuilder();
      buf.append(df4.format(cal.get(Calendar.YEAR)))
      buf.append("-")
      buf.append(df2.format(cal.get(Calendar.MONTH) + 1))
      buf.append("-")
      buf.append(df2.format(cal.get(Calendar.DAY_OF_MONTH)))
      buf.append("T")
      buf.append(df2.format(cal.get(Calendar.HOUR_OF_DAY)))
      buf.append(":")
      buf.append(df2.format(cal.get(Calendar.MINUTE)))
      buf.append(":")
      buf.append(df2.format(cal.get(Calendar.SECOND)))

      val ms = cal.get(Calendar.MILLISECOND)
      buf.append(".").append(df3.format(ms))

      var tzminute = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / 60000
      if (tzminute == 0)
        buf.append("Z")
      else {
        if (tzminute < 0) {
          tzminute = -tzminute
          buf.append("-")
        } else
          buf.append("+")
        val tzhour = tzminute / 60
        tzminute -= tzhour * 60
        buf.append(df2.format(tzhour))
        buf.append(":")
        buf.append(df2.format(tzminute));
      }
      buf.toString
    }

    /**
     * Converts the specified Date object to an RFC 3339 date/time string using
     * the specified time zone.
     *
     * @param date the Date object
     * @param zone the time zone to use
     * @return an RFC 3339 date/time string (does not include milliseconds)
     */
    def format(date: ju.Date, zone: TimeZone): String = {
      val cal = new GregorianCalendar(zone)
      cal.setTime(date)
      format(cal)
    }
  }

  object UTC extends DateFormat {
    /**
     * A time zone with zero offset and no DST.
     */
    private val UTCZone = new SimpleTimeZone(0, "Z")

    override def parse(source: String, pos: ParsePosition): ju.Date = {
      InternetDateFormat.parse(source).getTime
    }

    override def parse(source: String): ju.Date = parse(source, null)

    override def format(date: ju.Date, toAppendTo: StringBuffer,
                        fieldPosition: FieldPosition): StringBuffer =
      toAppendTo.append(InternetDateFormat.format(date, UTCZone))
  }

  object GMT extends DateFormat {
    /**
     * A time zone with zero offset and no DST.
     */
    private val GMTZone = TimeZone.getTimeZone("GMT")

    override def parse(source: String, pos: ParsePosition): ju.Date = InternetDateFormat.parse(source).getTime

    override def parse(source: String): ju.Date = parse(source, null)

    override def format(date: ju.Date, toAppendTo: StringBuffer, fieldPosition: FieldPosition): StringBuffer = {
      toAppendTo.append(InternetDateFormat.format(date, GMTZone))
    }
  }
}
