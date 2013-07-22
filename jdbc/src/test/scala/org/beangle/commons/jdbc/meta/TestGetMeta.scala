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
package org.beangle.commons.jdbc.meta

import scala.collection.JavaConversions._
import java.util.Map

object TestGetMeta {
  var personId: java.lang.Integer = _

  def main(args: Array[String]): Unit = {
    //    val ds: DataSource = new PoolingDataSourceFactory("org.h2.Driver",
    //      "jdbc:h2:/home/chaostone/eams/core/platform/eams-platform-doc/src/main/jdbc/eams;AUTO_SERVER=TRUE", "sa", "").getObject
    //    val database = new Database(ds.getConnection().getMetaData(), new H2Dialect(), null, "PUBLIC")
    //    database.loadTables(false)
    //    database.loadSequences
    //    listTableAndSequences(database)
    println(personId)
  }

  def listTableAndSequences(database: Database) = {
    val tables: Map[String, Table] = database.tables
    for (name <- tables.keySet()) {
      println("table " + name)
    }

    val seqs = database.loadSequences()
    for (obj <- seqs) println("sequence " + obj)
  }
}
