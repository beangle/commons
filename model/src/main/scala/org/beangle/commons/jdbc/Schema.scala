package org.beangle.commons.jdbc
import scala.collection.mutable
import org.beangle.commons.lang.Strings

class Schema(var database: Database, var name: Identifier) {

  var catalog: Option[Identifier] = None

  assert(null != name)

  val tables = new mutable.HashMap[String, Table]

  val sequences = new mutable.HashSet[Sequence]

  def createTable(tbname: String): Table = {
    tables.get(tbname) match {
      case Some(table) => table
      case None =>
        val ntable = new Table(this, database.engine.toIdentifier(tbname))
        tables.put(tbname, ntable)
        ntable
    }
  }
  /**
   * Using table literal (with or without schema) search table
   */
  def getTable(tbname: String): Option[Table] = {
    val engine = database.engine
    val nschema = this.name.toLiteral(engine)
    if (tbname.contains(".")) {
      if (nschema != engine.toIdentifier(Strings.substringBefore(tbname, ".")).value) None
      else tables.get(engine.toIdentifier(Strings.substringAfter(tbname, ".")).value)
    } else {
      tables.get(engine.toIdentifier(tbname).value)
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
    val seqMap = sequences.map(f => (f.qualifiedName, f)).toMap
    filter.filter(seqMap.keys).map { s => seqMap(s) }
  }

  override def toString: String = {
    "Schema " + name + " table:" + tables.keySet.toString + " sequence:" + sequences.toString
  }

  class NameFilter {
    val excludes = new collection.mutable.ListBuffer[String]
    val includes = new collection.mutable.ListBuffer[String]

    def filter(tables: Iterable[String]): List[String] = {
      val results = new collection.mutable.ListBuffer[String]
      for (tabame <- tables) {
        val tableName = if (tabame.contains(".")) Strings.substringAfter(tabame, ".") else tabame
        if (includes.exists(p => p == "*" || tableName.startsWith(p) && !excludes.contains(tableName)))
          results += tabame
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
