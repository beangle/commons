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
package org.beangle.commons.jdbc.query

import java.lang.reflect.Method
import java.sql.Connection
import java.sql.ParameterMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types._
import java.sql.Blob
import java.sql.Clob
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.io.StringReader
import java.io.InputStream
import java.math.BigDecimal

import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging

import javax.sql.DataSource

object JdbcExecutor {
  var oracleTimestampMethod: Method = _
  try {
    val clz = ClassLoaders.loadClass("oracle.sql.TIMESTAMP")
    oracleTimestampMethod = clz.getMethod("timestampValue")
  } catch {
    case e: Exception =>
  }
}

class JdbcExecutor(val dataSource: DataSource) extends Logging{

  var pmdKnownBroken: Boolean = false

  def queryForInt(sql: String): Int = query(sql).head.head.asInstanceOf[Number].intValue
  def queryForLong(sql: String): Long = query(sql).head.head.asInstanceOf[Number].longValue

  private def convertToSeq(rs: ResultSet): Seq[Seq[_]] = {
    val rows = new collection.mutable.ListBuffer[Seq[_]]
    val meta = rs.getMetaData()
    val cols = meta.getColumnCount()
    while (rs.next()) {
      rows += (for (i <- 0 until cols) yield {
        var v = rs.getObject(i + 1)
        if (null != v && meta.getColumnType(i + 1) == TIMESTAMP && !v.isInstanceOf[Timestamp]) {
          if (null != JdbcExecutor.oracleTimestampMethod)
            v = JdbcExecutor.oracleTimestampMethod.invoke(v)
          else throw new Exception("Cannot translate " + v.getClass + "timestamp to java.sql.Timestamp")
        }
        v
      })
    }
    rows
  }

  def query(sql: String, params: Any*): Seq[Seq[_]] = {
    val conn = getConnection()
    var stmt: PreparedStatement = null
    var rs: ResultSet = null
    try {
      stmt = conn.prepareStatement(sql)
      setParams(stmt, params, null)
      convertToSeq(stmt.executeQuery())
    } catch {
      case e: SQLException => rethrow(e, sql, params); List.empty
    } finally {
      stmt.close()
      conn.close()
    }

  }

  def update(sql: String, params: Any*): Int = {
    var stmt: PreparedStatement = null
    val conn = getConnection()
    var rows = 0
    try {
      stmt = conn.prepareStatement(sql)
      setParams(stmt, params, null)
      rows = stmt.executeUpdate()
    } catch {
      case e: SQLException => rethrow(e, sql, params)
    } finally {
      if(null!=stmt)stmt.close()
      conn.close()
    }
    rows
  }

  def getConnection(): Connection = dataSource.getConnection()

  def batch(sql: String, datas: Seq[Seq[_]], types: Seq[Int]): Seq[Int] = {
    var stmt: PreparedStatement = null
    val conn = getConnection()
    val rows = new collection.mutable.ListBuffer[Int]
    var curParam: Seq[_] = null
    try {
      stmt = conn.prepareStatement(sql)
      for (param <- datas) {
        curParam = param
        setParams(stmt, param, types)
        stmt.addBatch()
      }
      rows ++= stmt.executeBatch()
    } catch {
      case e: SQLException => rethrow(e, sql, curParam)
    } finally {
      stmt.close()
      conn.close()
    }
    rows.toList
  }

  def setParams(stmt: PreparedStatement, params: Seq[Any], types: Seq[Int]) {
    // check the parameter count, if we can
    val paramsCount = if (params == null) 0 else params.length
    var stmtCount = 0
    var sqltypes:Array[Int]=null

    if (null != types && !types.isEmpty ){
      stmtCount = types.length
      sqltypes = types.toArray
    } else {
      stmtCount = if (!pmdKnownBroken) stmt.getParameterMetaData().getParameterCount else params.length
      sqltypes=new Array[Int](stmtCount)
      for(i <- 0 until stmtCount) sqltypes(i)=NULL

      if(!pmdKnownBroken){
        var pmd = stmt.getParameterMetaData()
        try {
          for(i <- 0 until stmtCount) sqltypes(i)=pmd.getParameterType(i + 1)
        } catch {
          case e: SQLException =>  pmdKnownBroken = true
        }
      }
    }

    if (stmtCount > paramsCount)
      throw new SQLException("Wrong number of parameters: expected " + stmtCount + ", was given " + paramsCount)

    var i = 0
    while (i < stmtCount) {
      val index=i+1
      if (null==params(i)) {
        stmt.setNull(index,  if(sqltypes(i)==NULL) VARCHAR else sqltypes(i))
      }else{
        val value=params(i)
        try {
          sqltypes(i) match {
            case CHAR | VARCHAR =>
              stmt.setString(index, value.asInstanceOf[String]);
            case LONGVARCHAR =>
              stmt.setCharacterStream(index, new StringReader(value.asInstanceOf[String]));

            case BOOLEAN | BIT =>
              stmt.setBoolean(index, value.asInstanceOf[Boolean]);
            case TINYINT | SMALLINT | INTEGER =>
              stmt.setInt(index, value.asInstanceOf[Int]);
            case BIGINT =>
              stmt.setLong(index, value.asInstanceOf[Long]);

            case FLOAT | DOUBLE =>
              if (value.isInstanceOf[BigDecimal]) {
                stmt.setBigDecimal(index, value.asInstanceOf[BigDecimal]);
              } else {
                stmt.setDouble(index, value.asInstanceOf[Double]);
              }

            case NUMERIC | DECIMAL =>
              stmt.setBigDecimal(index, value.asInstanceOf[BigDecimal]);

            case DATE =>
              stmt.setDate(index, value.asInstanceOf[Date]);
            case TIMESTAMP =>
              stmt.setTimestamp(index, value.asInstanceOf[Timestamp]);

            case BINARY | VARBINARY | LONGVARBINARY =>
              stmt.setBinaryStream(index, value.asInstanceOf[InputStream]);

            case CLOB =>
              stmt.setAsciiStream(index,value.asInstanceOf[Clob].getAsciiStream)
            case BLOB =>
              stmt.setBinaryStream(index, value.asInstanceOf[Blob].getBinaryStream);
            case _ =>
              stmt.setObject(index,value)
          }
        } catch {
          case e: Exception => logger.error("set value error", e);
        }
      }
      
      i += 1
    }
  }

  protected def rethrow(cause: SQLException, sql: String, params: Any*) {
    var causeMessage = cause.getMessage()
    if (causeMessage == null) causeMessage = ""
    val msg = new StringBuffer(causeMessage)

    msg.append(" Query: ").append(sql).append(" Parameters: ")

    if (params == null)
      msg.append("[]")
    else
      msg.append(Strings.join(params, ","))

    val e = new SQLException(msg.toString(), cause.getSQLState(),
      cause.getErrorCode())
    e.setNextException(cause)
    throw e
  }
}
