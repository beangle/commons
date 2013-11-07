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

import org.beangle.commons.lang.Strings

class TableGrammarBean extends TableGrammar {

  var nullColumnString: String = ""
  var tableComment: String = null
  var columnComent: String = null
  var supportsUnique: Boolean = true
  var supportsNullUnique: Boolean = true
  var supportsColumnCheck: Boolean = true

  var dropSql: String = "drop table {}"

  var createString: String = "create table"

  def getComment(comment: String) =
    if (null == this.tableComment) "" else Strings.replace(this.tableComment, "{}", comment)

  def getColumnComment(comment: String) = {
    var newcomment = Strings.replace(comment,"'","")
    newcomment = Strings.replace(comment,"\"","")
    if (null == this.columnComent) ""
    else Strings.replace(this.columnComent, "{}", newcomment)
  }

  def dropCascade(table: String) = Strings.replace(dropSql, "{}", table)

}
