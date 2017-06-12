/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.cdi
import scala.language.implicitConversions

object Scope extends Enumeration {

  val Singleton = new Val("")

  val Prototype = new Val("prototype")

  val Request = new Val("request")

  val Session = new Val("session")

  class Val(var name: String) extends super.Val {
    override def toString(): String = name
  }

  implicit def convertValue(v: Value): Val = v.asInstanceOf[Val]
}
