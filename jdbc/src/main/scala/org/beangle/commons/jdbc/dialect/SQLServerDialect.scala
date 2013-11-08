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

class SQLServerDialect(version:String) extends AbstractTransactSQLDialect(version) {

  def this(){
    this("(,2000]")
  }

  protected override def registerType() = {
    super.registerType()
    registerType(LONGVARCHAR, "text")

    registerType(BIT, "bit")

    registerType(DATE, "datetime")
    registerType(TIME, "datetime")
    registerType(TIMESTAMP, "datetime")

    registerType(VARBINARY, "image")
    registerType(VARBINARY, 8000,"varbinary($l)")
    registerType(LONGVARBINARY, "image")
  }

  override def limitGrammar:LimitGrammar = null
  override def sequenceGrammar:SequenceGrammar = null
}
