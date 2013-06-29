/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect

import scala.collection.JavaConversions._
import java.util.Map

import org.beangle.commons.jdbc.meta.Database
import org.beangle.commons.jdbc.meta.Table
import org.beangle.commons.logging.Logging

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class DialectTestCase extends FlatSpec with ShouldMatchers with Logging{
  protected var dialect: Dialect = _
  protected var database: Database = _

  protected def listTableAndSequences = {
    val tables: Map[String, Table] = database.tables
    for (name <- tables.keySet()) {
      logger.info("table {}", name)
    }

    val seqs = database.sequences
    for (obj <- seqs) {
      logger.info("sequence {}", obj)
    }
  }
}
