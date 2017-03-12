/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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

import scala.collection.JavaConverters.asScalaSet
import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.reflect.runtime.{ universe => ru }

import org.beangle.commons.collection.Collections
import org.beangle.commons.jdbc.{ Column, Identifier }
import org.beangle.commons.lang.annotation.beta
import org.beangle.commons.lang.reflect.BeanInfos
import org.beangle.commons.logging.Logging
import org.beangle.commons.model.meta.Domain._
import org.beangle.commons.model.meta._
import org.beangle.commons.model.meta.EntityType

object MappingModule {

  val OrderColumnName = "idx"

  trait Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit
  }

  class NotNull extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      property.columns foreach (c => c.nullable = false)
    }
  }

  class Unique extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      property.columns foreach (c => c.unique = true)
    }
  }

  class ElementColumn(name: String) extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      val mp = cast[MapMapping](property, holder, "element column should used on map")
      mp.element.columns foreach (x => x.name = Identifier(name))
    }
  }

  //FIXME sqlType.length =>type name
  class ElementLength(len: Int) extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      val mp = cast[MapMapping](property, holder, "element length should used on map")
      mp.element.columns foreach (x => x.sqlType.length = Some(len))
    }
  }

  class Cache(val cacheholder: CacheHolder) extends Declaration {
    def apply(holder: EntityHolder[_], pm: PropertyMapping[_]): Unit = {
      val p = pm.property.asInstanceOf[Property]
      cacheholder.add(List(new Collection(holder.clazz, p.name)))
    }
  }

  class TypeSetter(val typeName: String) extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      cast[TypeNameHolder](property, holder, "TypeNameHolder needed").typeName = Some(typeName)
    }
  }

  private def refColumn(holder: EntityHolder[_], clazz: Class[_], entityName: String): Column = {
    val idType = BeanInfos.get(clazz).getPropertyType("id").get
    val mappings = holder.mappings
    new Column(mappings.database.engine.toIdentifier(columnName(entityName, true)), mappings.sqlTypeMapping.sqlType(idType), false)
  }

  class One2Many(targetEntity: Option[Class[_]], mappedBy: String, private var cascade: Option[String] = None) extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      val collp = cast[CollectionMapping](property, holder, "one2many should used on seq")
      collp.ownerColumn = refColumn(holder, holder.mapping.entity.clazz, holder.mapping.entity.entityName)
      val eleMapping = collp.element.asInstanceOf[EntityElementMapping]
      eleMapping.columns.clear
      eleMapping.one2many = true
      cascade foreach (c => collp.cascade = Some(c))
    }

    def cascade(c: String, orphanRemoval: Boolean = true): this.type = {
      this.cascade = Some(if (orphanRemoval && !c.contains("delete-orphan")) c + ",delete-orphan" else c)
      this
    }

    def cascaded: this.type = {
      this.cascade = Some("all,delete-orphan")
      this
    }
  }

  class OrderBy(orderBy: String) extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      val cm = cast[CollectionMapping](property, holder, "order by should used on seq");
      cm.property.asInstanceOf[CollectionPropertyImpl].orderBy = Some(orderBy)
    }
  }

  class Table(table: String) extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      cast[CollectionMapping](property, holder, "table should used on seq").table = Some(table)
    }
  }

  class ColumnName(name: String) extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      if (property.columns.size == 1) property.columns.head.name = Identifier(name)
    }
  }

  class OrderColumn(orderColumn: String) extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      val collp = cast[CollectionMapping](property, holder, "order column should used on many2many seq")
      val idxCol = new Column(Identifier(if (null != orderColumn) MappingModule.OrderColumnName else orderColumn), holder.mappings.sqlTypeMapping.sqlType(classOf[Int]), false)
      collp.index = Some(idxCol)
    }
  }

  class Length(len: Int) extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      property.columns foreach (c => c.sqlType.length = Some(len))
    }
  }

  class Target(clazz: Class[_]) extends Declaration {
    def apply(holder: EntityHolder[_], property: PropertyMapping[_]): Unit = {
      val sp = property.property.asInstanceOf[SingularPropertyImpl]
      sp.propertyType = holder.mappings.entities(clazz.getName)
    }
  }

  object Expression {
    // only apply unique on component properties
    def is(holder: EntityHolder[_], declarations: Seq[Declaration]): Unit = {
      val lasts = asScalaSet(holder.proxy.lastAccessed)
      if (!declarations.isEmpty && lasts.isEmpty) {
        throw new RuntimeException("Cannot find access properties for " + holder.mapping.entityName + " with declarations:" + declarations)
      }
      lasts foreach { property =>
        val p = holder.mapping.getProperty(property)
        declarations foreach (d => d(holder, p))
        p.mergeable = false
      }
      lasts.clear()
    }
  }

  class Expression(val holder: EntityHolder[_]) {

    def is(declarations: Declaration*): Unit = {
      Expression.is(holder, declarations)
    }

    def &(next: Expression): Expressions = {
      new Expressions(holder)
    }
  }

  class Expressions(val holder: EntityHolder[_]) {
    def &(next: Expression): this.type = {
      this
    }

    def are(declarations: Declaration*): Unit = {
      Expression.is(holder, declarations)
    }
  }

  final class EntityHolder[T](val mapping: EntityTypeMapping, val mappings: Mappings, val clazz: Class[T], module: MappingModule) {

    var proxy: Proxy.EntityProxy = _

    def cacheable(): this.type = {
      mapping.cache(module.cacheConfig.region, module.cacheConfig.usage)
      this
    }

    def cache(region: String): this.type = {
      mapping.cacheRegion = region
      this
    }

    def usage(usage: String): this.type = {
      mapping.cacheUsage = usage
      this
    }

    def on(declarations: T => Any)(implicit manifest: Manifest[T]): this.type = {
      if (null == proxy) proxy = Proxy.generate(clazz)
      declarations(proxy.asInstanceOf[T])
      this
    }

    def generator(strategy: String): this.type = {
      mapping.idGenerator = new IdGenerator(strategy)
      this
    }

    def table(table: String): this.type = {
      //FIXME
      //entity.table.name = table
      this
    }
  }

  final class CacheConfig(var region: String = null, var usage: String = null) {
  }

  final class CacheHolder(val mappings: Mappings, val cacheRegion: String, val cacheUsage: String) {
    def add(first: List[Collection], definitionLists: List[Collection]*): this.type = {
      first.foreach(d => mappings.addCollection(d.cache(cacheRegion, cacheUsage)))
      for (definitions <- definitionLists) {
        definitions.foreach(d => mappings.addCollection(d.cache(cacheRegion, cacheUsage)))
      }
      this
    }

    def add(first: Class[_ <: org.beangle.commons.model.Entity[_]], classes: Class[_ <: org.beangle.commons.model.Entity[_]]*): this.type = {
      mappings.getMapping(first).cache(cacheRegion, cacheUsage)
      for (clazz <- classes)
        mappings.getMapping(clazz).cache(cacheRegion, cacheUsage)
      this
    }
  }

  final class Entities(val entityMappings: collection.mutable.Map[String, EntityTypeMapping], cacheConfig: CacheConfig) {
    def except(clazzes: Class[_]*): this.type = {
      clazzes foreach { c => entityMappings -= c.getName }
      this
    }

    def cacheable(): Unit = {
      entityMappings foreach { e =>
        e._2.cacheRegion = cacheConfig.region
        e._2.cacheUsage = cacheConfig.usage
      }
    }

    def cache(region: String): this.type = {
      entityMappings foreach (e => e._2.cacheRegion = region)
      this
    }

    def usage(usage: String): this.type = {
      entityMappings foreach (e => e._2.cacheUsage = usage)
      this
    }
  }

  def columnName(propertyName: String, key: Boolean = false): String = {
    val lastDot = propertyName.lastIndexOf(".")
    val columnName = if (lastDot == -1) s"@${propertyName}" else "@" + propertyName.substring(lastDot + 1)
    if (key) columnName + "Id" else columnName
  }

  private def mismatch(msg: String, e: EntityTypeMapping, p: Property): Unit = {
    throw new RuntimeException(msg + s",Not for ${e.entityName}.${p.name}(${p.getClass.getSimpleName}/${p.clazz.getName})")
  }

  private def cast[T](pm: PropertyMapping[_], holder: EntityHolder[_], msg: String)(implicit manifest: Manifest[T]): T = {
    val p = pm.property.asInstanceOf[Property]
    if (!manifest.runtimeClass.isAssignableFrom(p.getClass)) mismatch(msg, holder.mapping, p)
    pm.asInstanceOf[T]
  }

  private def cast[T](p: Property, holder: EntityHolder[_], msg: String)(implicit manifest: Manifest[T]): T = {
    if (!manifest.runtimeClass.isAssignableFrom(p.getClass)) mismatch(msg, holder.mapping, p)
    p.asInstanceOf[T]
  }
}

@beta
abstract class MappingModule extends Logging {

  import MappingModule._
  private var currentHolder: EntityHolder[_] = _
  private var defaultIdGenerator: Option[String] = None
  private val cacheConfig = new CacheConfig()
  private val entityMappings = Collections.newMap[String, EntityTypeMapping]

  implicit def any2Expression(i: Any): Expression = {
    new Expression(currentHolder)
  }

  private var mappings: Mappings = _

  def binding(): Unit

  protected def declare[B](a: B*): Seq[B] = {
    a
  }

  protected def notnull = new NotNull

  protected def unique = new Unique

  protected def length(len: Int) = new Length(len)

  protected def cacheable: Cache = {
    new Cache(new CacheHolder(mappings, cacheConfig.region, cacheConfig.usage))
  }

  protected def cacheable(region: String, usage: String): Cache = {
    new Cache(new CacheHolder(mappings, region, usage))
  }

  protected def target[T](implicit manifest: Manifest[T]): Target = {
    new Target(manifest.runtimeClass)
  }

  protected def depends(clazz: Class[_], mappedBy: String): One2Many = {
    new One2Many(Some(clazz), mappedBy).cascaded
  }

  protected def depends(mappedBy: String): One2Many = {
    new One2Many(None, mappedBy).cascaded
  }

  protected def one2many(mappedBy: String): One2Many = {
    new One2Many(None, mappedBy)
  }

  protected def one2many(clazz: Class[_], mappedBy: String): One2Many = {
    new One2Many(Some(clazz), mappedBy)
  }

  protected def orderby(orderby: String): OrderBy = {
    new OrderBy(orderby)
  }

  protected def table(t: String): Table = {
    new Table(t)
  }

  protected def ordered: OrderColumn = {
    new OrderColumn(null)
  }

  protected def ordered(column: String): OrderColumn = {
    new OrderColumn(column)
  }

  protected def column(name: String): ColumnName = {
    new ColumnName(name)
  }

  protected def eleColumn(name: String): ElementColumn = {
    new ElementColumn(name)
  }

  protected def eleLength(len: Int): ElementLength = {
    new ElementLength(len)
  }

  protected def typeis(t: String): TypeSetter = {
    new TypeSetter(t)
  }

  protected final def bind[T: ClassTag]()(implicit manifest: Manifest[T], ttag: ru.TypeTag[T]): EntityHolder[T] = {
    bind(manifest.runtimeClass.asInstanceOf[Class[T]], null, ttag)
  }

  protected final def bind[T: ClassTag](entityName: String)(implicit manifest: Manifest[T], ttag: ru.TypeTag[T]): EntityHolder[T] = {
    bind(manifest.runtimeClass.asInstanceOf[Class[T]], entityName, ttag)
  }

  private def bind[T](cls: Class[T], entityName: String, ttag: ru.TypeTag[T]): EntityHolder[T] = {
    val mapping = mappings.autobind(cls, entityName, ttag.tpe)
    //find superclass's id generator
    var superCls: Class[_] = cls.getSuperclass
    while (null != superCls && superCls != classOf[Object]) {
      if (entityMappings.contains(superCls.getName)) {
        mapping.idGenerator = entityMappings(superCls.getName).idGenerator
        if (null != mapping.idGenerator) superCls = classOf[Object]
      }
      superCls = superCls.getSuperclass
    }

    if (null == mapping.idGenerator) {
      val unsaved = BeanInfos.get(cls, null).getPropertyType("id") match {
        case Some(idtype) => if (idtype.isPrimitive) "0" else "null"
        case None         => "null"
      }
      this.defaultIdGenerator foreach { a => mapping.idGenerator = new IdGenerator(a).unsaved(unsaved) }
    }
    mappings.addMapping(mapping)
    val holder = new EntityHolder(mapping, mappings, cls, this)
    currentHolder = holder
    entityMappings.put(mapping.entityName, mapping)
    holder
  }

  protected final def defaultIdGenerator(strategy: String): Unit = {
    defaultIdGenerator = Some(strategy)
  }

  protected final def cache(region: String): CacheHolder = {
    new CacheHolder(mappings, region, cacheConfig.usage)
  }

  protected final def cache(): CacheHolder = {
    new CacheHolder(mappings, cacheConfig.region, cacheConfig.usage)
  }

  protected final def all: Entities = {
    val newEntities = Collections.newMap[String, EntityTypeMapping]
    new Entities(newEntities ++ entityMappings, cacheConfig)
  }

  protected final def collection[T](properties: String*)(implicit manifest: Manifest[T]): List[Collection] = {
    val definitions = new scala.collection.mutable.ListBuffer[Collection]
    val clazz = manifest.runtimeClass
    properties foreach (p => definitions += new Collection(clazz, p))
    definitions.toList
  }

  protected final def defaultCache(region: String, usage: String) {
    cacheConfig.region = region
    cacheConfig.usage = usage
  }

  final def configure(mappings: Mappings): Unit = {
    logger.info(s"Process ${getClass.getName}")
    this.mappings = mappings
    this.binding()
    entityMappings.clear()
  }

  def typedef(name: String, clazz: String, params: Map[String, String] = Map.empty): Unit = {
    mappings.addType(name, clazz, params)
  }

  def typedef(forClass: Class[_], clazz: String): Unit = {
    mappings.addType(forClass.getName, clazz, Map.empty)
  }

  def typedef(forClass: Class[_], clazz: String, params: Map[String, String]): Unit = {
    mappings.addType(forClass.getName, clazz, params)
  }
}
