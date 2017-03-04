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
package org.beangle.commons.orm

import org.beangle.commons.model.meta._
import org.beangle.commons.jdbc.Table
import org.beangle.commons.jdbc.Column
import org.beangle.commons.collection.Collections
import scala.collection.mutable.Buffer
import org.beangle.commons.jdbc.SqlType
import java.sql.Types

class SimpleColumn(val column: Column) extends ColumnHolder with TypeNameHolder {
  columns += column
}

trait TypeNameHolder {
  var typeName: Option[String] = None
}

trait Fetchable {
  var fetch: Option[String] = None
}
trait ColumnHolder {
  var columns: Buffer[Column] = Buffer.empty[Column]
}

trait Mapping extends Cloneable {
  def typ: Type
  def copy(): Mapping
}

trait StructTypeMapping extends Mapping {
  var properties = Collections.newMap[String, PropertyMapping[_]]
  def getProperty(property: String): PropertyMapping[_] = {
    val idx = property.indexOf(".")
    if (idx == -1) {
      properties(property)
    } else {
      val sp = properties(property.substring(0, idx)).asInstanceOf[SingularMapping]
      sp.mapping.asInstanceOf[StructTypeMapping].getProperty(property.substring(idx + 1))
    }
  }
}

final class EntityTypeMapping(val typ: EntityType, val table: Table) extends StructTypeMapping {
  var cacheUsage: String = _
  var cacheRegion: String = _
  var isLazy: Boolean = true
  var proxy: String = _
  var isAbstract: Boolean = _
  var idGenerator: IdGenerator = _

  def cache(region: String, usage: String): this.type = {
    this.cacheRegion = region
    this.cacheUsage = usage
    this
  }

  def clazz: Class[_] = {
    typ.clazz
  }

  def entityName: String = {
    typ.entityName
  }
  def copy(): this.type = {
    this
  }
}

final class BasicTypeMapping(val typ: BasicType, column: Column, tpeName: String = null)
    extends Mapping with Cloneable with ColumnHolder with TypeNameHolder {

  if (null != column) columns += column
  if (null != tpeName) this.typeName = Some(tpeName)

  def copy(): BasicTypeMapping = {
    val cloned = super.clone().asInstanceOf[BasicTypeMapping]
    val cc = Buffer.empty[Column]
    columns foreach { c =>
      cc += c.clone()
    }
    cloned.columns = cc
    cloned
  }

}

final class EmbeddableTypeMapping(val typ: EmbeddableType) extends StructTypeMapping {

  def parentName: Option[String] = None

  def copy(): EmbeddableTypeMapping = {
    val cloned = super.clone().asInstanceOf[EmbeddableTypeMapping]
    val cp = Collections.newMap[String, PropertyMapping[_]]
    properties foreach {
      case (name, p) =>
        cp += (name -> p.copy().asInstanceOf[PropertyMapping[Property]])
    }
    cloned.properties = cp
    cloned
  }

}

abstract class PropertyMapping[T <: Property](val property: T) extends TypeNameHolder {
  var access: Option[String] = None
  var cascade: Option[String] = None
  var mergeable: Boolean = true

  var updateable: Boolean = true
  var insertable: Boolean = true
  var optimisticLocked: Boolean = true
  var lazyed: Boolean = false
  var generator: IdGenerator = _
  var generated: Option[String] = None

  def copy(): this.type
}

final class SingularMapping(property: SingularProperty, var mapping: Mapping)
    extends PropertyMapping(property) with Fetchable {
  def copy: this.type = {
    val cloned = super.clone().asInstanceOf[this.type]
    cloned.mapping = this.mapping.copy()
    cloned
  }
}

abstract class PluralMapping[T <: PluralProperty](property: T, var element: Mapping)
    extends PropertyMapping(property) with Fetchable {
  var ownerColumn: Column = _
  var inverse: Boolean = false
  var where: Option[String] = None
  var batchSize: Option[Int] = None
  var index: Option[Column] = None
  var table: Option[String] = None
  var subselect: Option[String] = None
  var sort: Option[String] = None

  var one2many = false
  def many2many: Boolean = !one2many

  def copy: this.type = {
    val cloned = super.clone().asInstanceOf[this.type]
    cloned.element = this.element.copy()
    cloned
  }
}

class CollectionMapping(property: CollectionProperty, element: Mapping) extends PluralMapping(property, element)

final class MapMapping(property: MapProperty, var key: Mapping, element: Mapping)
    extends PluralMapping(property, element) {

  override def copy(): this.type = {
    val cloned = super.clone().asInstanceOf[this.type]
    cloned.element = this.element.copy()
    cloned.key = this.key.copy()
    cloned
  }
}

class TypeDef(val clazz: String, val params: Map[String, String])

final class Collection(val clazz: Class[_], val property: String) {
  var cacheRegion: String = _
  var cacheUsage: String = _

  def cache(region: String, usage: String): this.type = {
    this.cacheRegion = region
    this.cacheUsage = usage
    this
  }
}

final class IdGenerator(var name: String) {
  val params = Collections.newMap[String, String]
  var nullValue: Option[String] = None

  def unsaved(value: String): this.type = {
    nullValue = Some(value)
    this
  }
}
