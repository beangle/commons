/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.jdbc.meta.dialect

import scala.collection.JavaConversions._
import java.util.Map

import org.beangle.jdbc.meta.model.Database
import org.beangle.jdbc.meta.model.Table
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class DialectTestCase extends FlatSpec with ShouldMatchers {
  protected var dialect: Dialect = _
  protected var database: Database = _

  val logger: Logger = LoggerFactory.getLogger(classOf[DialectTestCase])

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
