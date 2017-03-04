package org.beangle.commons.orm

import org.beangle.commons.jdbc.Engine
import org.beangle.commons.jdbc.SqlType
import java.sql.Types

object SqlTypeMapping {
  def DefaultStringSqlType = new SqlType(Types.VARCHAR, "varchar(255)", 255)
}

class SqlTypeMapping(engine: Engine) {
  def sqlType(clazz: Class[_]): SqlType = {
    null
  }
}
