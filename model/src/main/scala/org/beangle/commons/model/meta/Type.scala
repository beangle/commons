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
package org.beangle.commons.model.meta

import org.beangle.commons.collection.Collections

trait Type {
  def clazz: Class[_]

  def newInstance(): AnyRef = {
    clazz.newInstance().asInstanceOf[AnyRef]
  }
}

class BasicType(val clazz: Class[_]) extends Type

trait StructType extends Type {
  def getProperty(property: String): Option[Property]
}

trait EmbeddableType extends StructType {
  def parentName: Option[String]
}

trait EntityType extends StructType {
  def id: Property
  def entityName: String
}

trait Property {
  def name: String
  def clazz: Class[_]
  def optional: Boolean
}
trait SingularProperty extends Property {
  def propertyType: Type
}

trait PluralProperty extends Property {
  def element: Type
}

trait CollectionProperty extends PluralProperty {
  def orderBy: Option[String]
}

trait MapProperty extends PluralProperty {
  def key: Type
}
