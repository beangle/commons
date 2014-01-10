/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.jdbc.dialect

import org.beangle.commons.lang.Strings

trait TableGrammar {

  def nullColumnString: String

  def createString: String

  def getComment(comment: String): String

  def dropCascade(table: String): String

  def getColumnComment(comment: String): String

  def supportsUnique: Boolean

  def supportsNullUnique: Boolean

  def supportsColumnCheck: Boolean
}

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
    if (null == this.columnComent) ""
    else {
      var newcomment = Strings.replace(comment, "'", "")
      newcomment = Strings.replace(newcomment, "\"", "")
      Strings.replace(this.columnComent, "{}", newcomment)
    }
  }

  def dropCascade(table: String) = Strings.replace(dropSql, "{}", table)

}

trait LimitGrammar {

  /**
   * ANSI SQL defines the LIMIT clause to be in the form LIMIT offset, limit.
   * Does this dialect require us to bind the parameters in reverse order?
   *
   * @return true if the correct order is limit, offset
   */
  def bindInReverseOrder: Boolean

  def bindFirst: Boolean

  def useMax: Boolean

  def limit(query: String, hasOffset: Boolean): String

}

class LimitGrammarBean(pattern: String, offsetPattern: String, val bindInReverseOrder: Boolean,
    val bindFirst: Boolean, val useMax: Boolean) extends LimitGrammar {

  def limit(query: String, hasOffset: Boolean) =
    if (hasOffset) Strings.replace(offsetPattern, "{}", query) else Strings.replace(pattern, "{}", query)
}

/**
 * sequence grammar
 * @author chaostone
 *
 */
class SequenceGrammar {

  var createSql: String = "create sequence :name start with :start increment by :increment"
  var dropSql: String = "drop sequence :name"
  var nextValSql: String = null
  var selectNextValSql: String = null
  var querySequenceSql: String = null

}
