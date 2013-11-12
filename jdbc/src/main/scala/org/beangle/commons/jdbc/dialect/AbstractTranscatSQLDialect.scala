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
package org.beangle.commons.jdbc.dialect

import java.sql.Types._

abstract class AbstractTransactSQLDialect(version: String) extends AbstractDialect(version) {

  val SELECT: String = "select"
  val FROM: String = "from"
  val DISTINCT: String = "distinct"

  override def sequenceGrammar: SequenceGrammar = null

  protected override def registerType = {
    registerType(CHAR, "char($l)")
    registerType(VARCHAR, "varchar($l)")

    registerType(BIT, "tinyint")
    registerType(BIGINT, "numeric(19,0)")
    registerType(SMALLINT, "smallint")
    registerType(TINYINT, "smallint")
    registerType(INTEGER, "int")
    registerType(FLOAT, "float")
    registerType(DECIMAL, "double precision")
    registerType(DOUBLE, "double precision")
    registerType(NUMERIC, "numeric($p,$s)")

    registerType(DATE, "datetime")
    registerType(TIME, "datetime")
    registerType(TIMESTAMP, "datetime")

    registerType(BINARY, "binary")
    registerType(VARBINARY, "varbinary($l)")
    registerType(LONGVARBINARY, "varbinary($l)")
    registerType(BLOB, "image")
    registerType(CLOB, "text")
  }

}
