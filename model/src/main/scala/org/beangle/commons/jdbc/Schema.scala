package org.beangle.commons.jdbc
import scala.collection.mutable
import org.beangle.commons.lang.Strings

class Schema(var database: Database, var name: Identifier) {

  var catalog: Option[Identifier] = None

  assert(null != name)

  val tables = new mutable.HashMap[Identifier, Table]

  val sequences = new mutable.HashSet[Sequence]

  def createTable(tbname: String): Table = {
    val tableId = Identifier(tbname)
    tables.get(tableId) match {
      case Some(table) => table
      case None =>
        val ntable = new Table(this, database.engine.toIdentifier(tbname))
        tables.put(tableId, ntable)
        ntable
    }
  }
  /**
   * Using table literal (with or without schema) search table
   */
  def getTable(tbname: String): Option[Table] = {
    val engine = database.engine
    val nschema = name.toLiteral(engine)
    if (tbname.contains(".")) {
      if (nschema != engine.toIdentifier(Strings.substringBefore(tbname, ".")).value) None
      else tables.get(engine.toIdentifier(Strings.substringAfter(tbname, ".")))
    } else {
      tables.get(engine.toIdentifier(tbname))
    }
  }
  def filterTables(includes: Seq[String], excludes: Seq[String]): Seq[Table] = {
    val filter = new NameFilter()
    val engine = database.engine
    if (null != includes) {
      for (include <- includes) filter.include(engine.toIdentifier(include).value)
    }
    if (null != excludes) {
      for (exclude <- excludes) filter.exclude(engine.toIdentifier(exclude).value)
    }

    filter.filter(tables.keySet).map { t => tables(t) }
  }

  def filterSequences(includes: Seq[String], excludes: Seq[String]): Seq[Sequence] = {
    val engine = database.engine
    val filter = new NameFilter()
    if (null != includes) {
      for (include <- includes) filter.include(engine.toIdentifier(include).value)
    }
    if (null != excludes) {
      for (exclude <- excludes) filter.exclude(engine.toIdentifier(exclude).value)
    }
    val seqMap = sequences.map(f => (f.name, f)).toMap
    filter.filter(seqMap.keys).map { s => seqMap(s) }
  }

  override def toString: String = {
    "Schema " + name + " table:" + tables.keySet.toString + " sequence:" + sequences.toString
  }

  class NameFilter {
    val excludes = new collection.mutable.ListBuffer[String]
    val includes = new collection.mutable.ListBuffer[String]

    def filter(tables: Iterable[Identifier]): List[Identifier] = {
      val results = new collection.mutable.ListBuffer[Identifier]
      for (tabId <- tables) {
        val tabame = tabId.value
        val tableName = if (tabame.contains(".")) Strings.substringAfter(tabame, ".") else tabame
        if (includes.exists(p => p == "*" || tableName.startsWith(p) && !excludes.contains(tableName)))
          results += tabId
      }
      results.toList
    }

    def exclude(table: String) {
      excludes += table
    }

    def include(table: String) {
      includes += table
    }
  }

}
