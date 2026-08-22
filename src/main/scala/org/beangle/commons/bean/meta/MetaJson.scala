/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.bean.meta

import org.beangle.commons.bean.meta.MetaModel.ClassMeta
import org.beangle.commons.json.JsonObject

/** JSON serialization of [[MetaModel.ClassMeta]] (debug export only; no reverse parse). */
object MetaJson {

  /** Renders a ClassMeta as human-readable JSON.
    *
    * Types use `TypeInfo.name` (Scala-style, e.g. `Option[String]`); ctor defaults
    * are rendered as strings (None → null).
    */
  def toJson(cm: ClassMeta): String = {
    JsonObject(
      "clazz" -> cm.clazz.getName,
      "properties" -> cm.properties.map(p => JsonObject(
        "name" -> p.name,
        "type" -> p.typeinfo.name,
        "transient" -> p.isTransient,
        "optional" -> p.isOptional)),
      "ctors" -> cm.ctors.map(c => JsonObject(
        "parameters" -> c.parameters.map(pm => JsonObject(
          "name" -> pm.name,
          "type" -> pm.typeinfo.name,
          "default" -> pm.defaultValue.map(v => if null == v then null else v.toString))))),
      "methods" -> cm.methods.map(m => JsonObject(
        "name" -> m.name,
        "params" -> m.paramTypes))
    ).toJson
  }

  /** CLI: prints each .beaninfo file as JSON (debug). */
  def main(args: Array[String]): Unit = {
    if args.isEmpty then println("Usage: MetaJson <file.beaninfo>...")
    else args foreach { path =>
      val bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path))
      println(toJson(MetaCodec.parse(bytes)))
    }
  }
}
