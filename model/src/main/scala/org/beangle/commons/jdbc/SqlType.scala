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
    } else {
      this.length = Some(length)
    }
  }

  def this(code: Int, name: String, length: Int, scale: Int) {
    this(code, name)
    if (SqlType.isNumberType(code)) {
      this.precision = Some(length)
      this.scale = Some(scale)
    } else {
      this.length = Some(length)
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
}
