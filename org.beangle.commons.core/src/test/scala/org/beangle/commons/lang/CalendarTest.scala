/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

import java.util.Calendar
import java.util.Date

/**
 * @author chaostone
 * @version $Id: CalendarTest.java Jul 26, 2011 4:12:17 PM chaostone $
 */

class CalendarTest  extends FunSpec with ShouldMatchers{

  describe("Dates"){
    it("Roll minutes") {
      val calendar = Calendar.getInstance
      val ajusted = Dates.rollMinutes(calendar.getTime, -30)
      ajusted.before(calendar.getTime) should be (true)
    }
  }
}
