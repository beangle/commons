/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect

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
