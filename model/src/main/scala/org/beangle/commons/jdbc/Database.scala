package org.beangle.commons.jdbc

class Database(val engine: Engine) {

  var schemas = new collection.mutable.HashMap[Identifier, Schema]

  def getOrCreateSchema(schema: Identifier): Schema = {
    schemas.getOrElseUpdate(schema, new Schema(this, schema))
  }

  def getOrCreateSchema(schema: String): Schema = {
    getOrCreateSchema(engine.toIdentifier(schema))
  }
}
