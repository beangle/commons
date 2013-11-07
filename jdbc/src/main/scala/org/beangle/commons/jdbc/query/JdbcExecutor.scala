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
import java.sql.Types

import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.Strings

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

class JdbcExecutor(val dataSource: DataSource) {

  var pmdKnownBroken: Boolean = false

  def queryForInt(sql: String): Int = query(sql).head.head.asInstanceOf[Number].intValue

  private def convertToSeq(rs: ResultSet): Seq[Seq[_]] = {
    val rows = new collection.mutable.ListBuffer[Seq[_]]
    val meta = rs.getMetaData()
    val cols = meta.getColumnCount()
    while (rs.next()) {
      rows += (for (i <- 0 until cols) yield {
        var v = rs.getObject(i + 1)
        if (null != v && meta.getColumnType(i + 1) == Types.TIMESTAMP && !v.isInstanceOf[Timestamp]) {
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
      fillStatement(stmt, params, null)
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
      fillStatement(stmt, params, null)
      stmt.addBatch()
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
        fillStatement(stmt, param, types)
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

  def fillStatement(stmt: PreparedStatement, params: Seq[Any], types: Seq[Int]) {
    // check the parameter count, if we can
    var pmd: ParameterMetaData = null
    var stmtCount = 0
    if (null != types) stmtCount = types.length
    if (!pmdKnownBroken) {
      pmd = stmt.getParameterMetaData()
      stmtCount = pmd.getParameterCount()
      val paramsCount = if (params == null) 0 else params.length
      if (stmtCount > paramsCount) {
        throw new SQLException("Wrong number of parameters: expected "
          + stmtCount + ", was given " + paramsCount)
      }
    }
    var i = 0
    while (i < stmtCount) {
      if (params(i) != null) {
        //if (null != types && !types.isEmpty)
        //  StatementUtils.setValue(stmt, i + 1, params(i).asInstanceOf[Object], types(i));
        //else
        stmt.setObject(i + 1, params(i))
      } else {
        // VARCHAR works with many drivers regardless
        // of the actual column type. Oddly, NULL and
        // OTHER don't work with Oracle's drivers.
        var sqlType = Types.VARCHAR
        if (!pmdKnownBroken) {
          try {
            sqlType = pmd.getParameterType(i + 1)
          } catch {
            case e: SQLException =>
              pmdKnownBroken = true
          }
        }
        stmt.setNull(i + 1, sqlType)
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
