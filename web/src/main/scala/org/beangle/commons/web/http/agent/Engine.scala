/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.web.http.agent

object Engines extends Enumeration {

  val Trident = new Engine("Trident")

  val Gecko = new Engine("Gecko")

  val WebKit = new Engine("WebKit")

  val Presto = new Engine("Presto")

  val Mozilla = new Engine("Mozilla")

  val Khtml = new Engine("KHTML")

  val Word = new Engine("Microsoft Office Word")

  val Other = new Engine("Other")

  class Engine(var name: String) extends Val {

    var categories: List[Browsers.Category] = Nil

    def addCategory(category: Browsers.Category) {
      categories = categories ::: List(category)
    }
  }

  import scala.language.implicitConversions
  implicit def convertValue(v: Value): Engine = v.asInstanceOf[Engine]
}
