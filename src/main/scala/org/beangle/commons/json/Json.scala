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
  def parse(s: String): Json = {
    if Strings.isBlank(s) then JsonValue("")
    else new JsonParser(new java.io.StringReader(s)).parse()
  }

  def query(s: String, path: String): Option[Any] = {
    parse(s).query(path)
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

  def empty(t: Class[_]): Json = {
    if (t == classOf[JsonObject]) new JsonObject()
    else if (t == classOf[JsonArray]) new JsonArray()
    else JsonValue("")
  }
}

trait Json {
  /** Query object graph,using slash or dot seperate nested property,like
   * [index]/property_name/property_name or [index].property_name.property_name
   *
   * @param path
   * @return
   */
  def query(path: String): Option[Any]

  def toJson:String

  def value: Any
}
