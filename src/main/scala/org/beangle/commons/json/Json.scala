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

package org.beangle.commons.json

import org.beangle.commons.lang.Strings

object Json {
  def parse(s: String): Any = {
    val parser = new JsonParser(new java.io.StringReader(s))
    parser.parse()
  }

  def parseObject(s: String): JsonObject = {
    if Strings.isBlank(s) then new JsonObject() else parse(s).asInstanceOf[JsonObject]
  }

  def parseArray(s: String): JsonArray = {
    if Strings.isBlank(s) then new JsonArray() else parse(s).asInstanceOf[JsonArray]
  }

  def toJson(datas: collection.Map[String, Any]): String = {
    if (datas.isEmpty) "" else new JsonObject(datas).toJson
  }

  def toJson(value: JsonObject): String = {
    value.toJson
  }

  def toJson(value: JsonArray): String = {
    value.toJson
  }
}
