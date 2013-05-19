/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.util

import java.sql.Types._
import java.io.InputStream
import java.math.BigDecimal
import java.sql.Blob
import java.sql.Clob
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.StringReader

class StatementUtils

object StatementUtils {

  val logger: Logger = LoggerFactory.getLogger(classOf[StatementUtils]);

  def setValue(ps: PreparedStatement, index: Int, value: Object, sqlType: Int) = {
    if (null == value) {
      ps.setNull(index, sqlType);
    } else {
      try {
        sqlType match {
          case CHAR | VARCHAR =>
            ps.setString(index, value.asInstanceOf[String]);
          case LONGVARCHAR =>
            ps.setCharacterStream(index, new StringReader(value.asInstanceOf[String]));

          case BOOLEAN | BIT =>
            ps.setBoolean(index, value.asInstanceOf[Boolean]);
          case TINYINT | SMALLINT | INTEGER =>
            ps.setInt(index, value.asInstanceOf[Int]);
          case BIGINT =>
            ps.setLong(index, value.asInstanceOf[Long]);

          case FLOAT | DOUBLE =>
            if (value.isInstanceOf[BigDecimal]) {
              ps.setBigDecimal(index, value.asInstanceOf[BigDecimal]);
            } else {
              ps.setDouble(index, value.asInstanceOf[Double]);
            }

          case NUMERIC | DECIMAL =>
            ps.setBigDecimal(index, value.asInstanceOf[BigDecimal]);

          case DATE =>
            ps.setDate(index, value.asInstanceOf[Date]);
          case TIMESTAMP =>
            ps.setTimestamp(index, value.asInstanceOf[Timestamp]);

          case BINARY | VARBINARY | LONGVARBINARY =>
            ps.setBinaryStream(index, value.asInstanceOf[InputStream]);

          case CLOB =>
            ps.setClob(index, value.asInstanceOf[Clob]);
          case BLOB =>
            ps.setBlob(index, value.asInstanceOf[Blob]);
          case _ =>
            logger.warn("unsupported type {}", sqlType);
        }
      } catch {
        case e: Exception => {
          logger.error("set value error", e);
          e.printStackTrace();
        }
      }
    }
  }
}
