package org.beangle.commons.jdbc

import org.beangle.commons.lang.Strings

class Database(val engine: Engine) {

  var schemas = new collection.mutable.HashMap[Identifier, Schema]

  def getOrCreateSchema(schema: Identifier): Schema = {
    schemas.getOrElseUpdate(schema, new Schema(this, schema))
  }

  def getOrCreateSchema(schema: String): Schema = {
    if (Strings.isEmpty(schema)) {
      getOrCreateSchema(Identifier.empty)
    } else {
      getOrCreateSchema(engine.toIdentifier(schema))
    }
  }
}
