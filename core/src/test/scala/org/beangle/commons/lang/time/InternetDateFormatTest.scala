package org.beangle.commons.lang.time

import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import java.text.SimpleDateFormat

@RunWith(classOf[JUnitRunner])
class InternetDateFormatTest extends FunSpec with Matchers {
  describe("InternetDateFormat") {
    it("format") {
      val date = new java.util.Date()
      val utcString = UTCFormat.format(date)
      val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
      val newDate = format.format(date)
      assert(utcString == newDate)

      val gmtString = GMTFormat.format(date)
      println(gmtString)
    }
    it("parse") {
      val s = "2014-12-17T04:37:15.230Z"
      val date1 = UTCFormat.parse("2014-12-17T04:37:15.230Z")
      val date2 = UTCFormat.parse("2014-12-17 04:37:15.230Z")
      assert(date1 == date2)
    }
  }
}
