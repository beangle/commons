/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.message

import java.util.List
import java.util.Properties
import Message._
//remove if not needed
import scala.collection.JavaConversions._

object Message {
  val TEXT = "text/plain charset=UTF-8"

  val HTML = "text/html charset=UTF-8"
}

trait Message {

  def getSubject(): String

  def setSubject(subject: String): Unit

  def getText(): String

  def setText(text: String): Unit

  def getProperties(): Properties

  def getRecipients(): List[String]

  def getContentType(): String

  def setContentType(contentType: String): Unit
}
