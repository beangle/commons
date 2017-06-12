/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.jdbc

import java.sql.Types._

object SqlType {
  val numberTypes = Set(TINYINT, SMALLINT, INTEGER, BIGINT, FLOAT, REAL, DOUBLE, NUMERIC, DECIMAL)

  def isNumberType(code: Int): Boolean = {
    numberTypes.contains(code)
  }
}

class SqlType(var code: Int, var name: String) {

  def this(code: Int, name: String, length: Int) {
    this(code, name)
    if (SqlType.isNumberType(code)) {
      this.precision = Some(length)
      this.scale = Some(0)
      this.length = None
    } else {
      if (length > 0) this.length = Some(length)
    }
  }

  def this(code: Int, name: String, length: Int, scale: Int) {
    this(code, name)
    if (SqlType.isNumberType(code)) {
      this.precision = Some(length)
      this.scale = Some(scale)
      this.length = None
    } else {
      if (length > 0) this.length = Some(length)
    }
  }

  def isNumberType: Boolean = {
    SqlType.isNumberType(code)
  }
  /**
   *  Charactor length
   */
  var length: Option[Int] = None

  /**
   *  numeric precision
   *  The number 123.45 has a precision of 5 and a scale of 2
   */
  var precision: Option[Int] = None

  var scale: Option[Int] = None

  override def toString: String = {
    name
  }
}
