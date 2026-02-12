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

package org.beangle.commons.conversion.string

import org.beangle.commons.conversion.Converter
import org.beangle.commons.json.{Json, JsonArray, JsonObject}

/** Converts string to Json/JsonObject/JsonArray.
 *
 * @author chaostone
 * @since 5.7.0
 */
object JsonConverter extends StringConverterFactory[String, Json] {

  register(classOf[JsonObject], new JsonObjectConverter())
  register(classOf[JsonArray], new JsonArrayConverter())
  register(classOf[Json], new JsonConverter())

  private class JsonObjectConverter extends Converter[String, JsonObject] {
    override def apply(input: String): JsonObject = {
      Json.parseObject(input)
    }
  }

  private class JsonArrayConverter extends Converter[String, JsonArray] {
    override def apply(input: String): JsonArray = {
      Json.parseArray(input)
    }
  }

  private class JsonConverter extends Converter[String, Json] {
    override def apply(input: String): Json = {
      Json.parse(input)
    }
  }
}
