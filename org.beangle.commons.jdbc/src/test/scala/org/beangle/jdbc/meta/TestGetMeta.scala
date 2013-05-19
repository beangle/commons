package org.beangle.jdbc.meta

import org.beangle.jdbc.meta.model.Database
import org.beangle.jdbc.meta.model.Table

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
