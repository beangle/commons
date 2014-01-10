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

import java.util.Properties

import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings
//remove if not needed
import scala.collection.JavaConversions._

abstract class AbstractMessage extends Message {

  private var subject: String = _

  private var text: String = _

  private var properties: Properties = new Properties

  private var contentType: String = Message.TEXT

  def getSubject(): String = this.subject

  def setSubject(subject: String) = { this.subject = subject }

  def getText(): String = this.text

  def setText(text: String) = { this.text = text }

  def getProperties(): Properties = this.properties

  def setProperties(properties: Properties) = { this.properties = properties }

  def getContentType(): String = this.contentType

  def setContentType(contentType: String) = {
    Assert.notEmpty(contentType)
    Assert.isTrue(Strings.contains(contentType, "charset="), "contentType should contain charset")
    this.contentType = contentType
  }
}