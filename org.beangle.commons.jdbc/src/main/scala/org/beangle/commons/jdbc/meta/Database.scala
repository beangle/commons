/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.meta

import scala.collection.mutable
import java.sql.{ SQLException, DatabaseMetaData }
import org.beangle.commons.jdbc.dialect.Dialect

/**
 * JDBC database metadata
 *
 * @author chaostone
 */
class Database(meta: DatabaseMetaData, val dialect: Dialect, val catalog: String, val schema: String) {

  val tables = if (meta.storesMixedCaseIdentifiers) new mutable.HashMap[String, Table] else new CaseInsensitiveMap[Table]

  val sequences = new mutable.HashSet[Sequence]

  def loadTables(extras: Boolean): mutable.HashMap[String, Table] = {
    val loader: MetadataLoader = new MetadataLoader(dialect, meta)
    val loadTables: Set[Table] = loader.loadTables(catalog, schema, extras)
    for (table <- loadTables) {
      tables.put(table.identifier, table)
    }
    tables
  }

  def loadSequences(): mutable.HashSet[Sequence] = {
    val loader: MetadataLoader = new MetadataLoader(dialect, meta)
    sequences ++= loader.loadSequences(meta.getConnection(), schema)
    sequences
  }

  def getTable(name: String) = tables.get(name)

  override def toString = "Database" + tables.keySet.toString() + sequences.toString()
}

class CaseInsensitiveMap[V] extends mutable.HashMap[String, V] {
  override def put(key: String, value: V) = super.put(key.toLowerCase, value)

  override def get(key: String): Option[V] = return super.get(key.toString.toLowerCase)

  override def remove(key: String): Option[V] = { return super.remove(key.toString().toLowerCase) }
}
