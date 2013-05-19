/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General def License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect

abstract class TableGrammar {

  def nullColumnString: String

  def createString: String

  def getComment(comment: String): String

  def dropCascade(table: String): String

  def getColumnComment(comment: String): String

  def supportsUnique: Boolean

  def supportsNullUnique: Boolean

  def supportsColumnCheck: Boolean
}
