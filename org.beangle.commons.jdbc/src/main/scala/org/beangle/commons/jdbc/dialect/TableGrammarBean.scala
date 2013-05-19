/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
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
    if (null == this.columnComent) ""
    else Strings.replace(this.columnComent, "{}", comment)
  }

  def dropCascade(table: String) = Strings.replace(dropSql, "{}", table)

}
