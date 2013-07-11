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
package org.beangle.commons.csv

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class CsvTest extends FunSpec with ShouldMatchers {

  describe("CsvFormat") {
    it("Format") {
      val builder = new CsvFormat.Builder
      builder.separator(CsvConstants.Comma).separator(CsvConstants.Semicolon)
        .delimiter(CsvConstants.Quote)
      val csv = new Csv(builder.build())
      csv.format.isSeparator(CsvConstants.Comma) should be(true)
      csv.format.isSeparator(CsvConstants.Semicolon) should be(true)
      csv.format.delimiter should equal(CsvConstants.Quote)
    }
  }

}
