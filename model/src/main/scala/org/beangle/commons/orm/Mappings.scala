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

import java.lang.reflect.Modifier

import scala.collection.mutable
import scala.reflect.runtime.{ universe => ru }

import org.beangle.commons.collection.Collections
import org.beangle.commons.jdbc.{ Column, Database }
import org.beangle.commons.lang.{ ClassLoaders, Primitives, Strings }
import org.beangle.commons.lang.annotation.value
import org.beangle.commons.lang.reflect.BeanInfos
import org.beangle.commons.logging.Logging
import org.beangle.commons.orm._
import org.beangle.commons.model.meta._
import org.beangle.commons.model.meta.Domain._
import org.beangle.commons.orm.Jpas.{ isComponent, isEntity, isMap, isSeq, isSet }

object Mappings {
  case class Holder(mapping: EntityTypeMapping, meta: MutableStructType)
  /**
   * @param key 表示是否是一个外键
   * @return @propertyName @用以区分是否需要采用命名策略再次命名
   */
  def columnName(propertyName: String, key: Boolean = false): String = {
    val lastDot = propertyName.lastIndexOf(".")
    val columnName = if (lastDot == -1) s"@${propertyName}" else "@" + propertyName.substring(lastDot + 1)
    if (key) columnName + "Id" else columnName
  }
}

import Mappings._
final class Mappings(val database: Database, val namingPolicy: NamingPolicy) extends Logging {

  var sqlTypeMapping = new SqlTypeMapping(database.engine)

  val entities = new mutable.HashMap[String, EntityTypeImpl]

  /**
   * all type mappings(clazz -> Entity)
   */
  val classMappings = new mutable.HashMap[Class[_], EntityTypeMapping]

  /**
   * custome types
   */
  val types = new mutable.HashMap[String, TypeDef]

  /**
   * Buildin value types
   */
  val valueTypes = new mutable.HashSet[Class[_]]

  /**
   * Buildin enum types
   */
  val enumTypes = new mutable.HashMap[String, String]

  /**
   * Classname.property -> Collection
   */
  val collectMap = new mutable.HashMap[String, Collection]

  /**
   * Only entities
   */
  val entityMappings = Collections.newMap[String, EntityTypeMapping]

  def collections: Iterable[Collection] = collectMap.values

  def getMapping(clazz: Class[_]): EntityTypeMapping = classMappings(clazz)

  def addMapping(mapping: EntityTypeMapping): this.type = {
    val cls = mapping.clazz
    classMappings.put(cls, mapping)
    if (!cls.isInterface() && !Modifier.isAbstract(cls.getModifiers)) {
      //replace super entity with same entityName
      //It's very strange,hibnerate ClassMetadata with has same entityName and mappedClass in type overriding,
      //So, we leave  hibernate a  clean world.
      entityMappings.get(mapping.entityName) match {
        case Some(o) => if (o.clazz.isAssignableFrom(mapping.clazz)) entityMappings.put(mapping.entityName, mapping)
        case None    => entityMappings.put(mapping.entityName, mapping)
      }
    }
    this
  }

  def addCollection(definition: Collection): this.type = {
    collectMap.put(definition.clazz.getName() + definition.property, definition)
    this
  }

  def addType(name: String, clazz: String, params: Map[String, String]): Unit = {
    types.put(name, new TypeDef(clazz, params))
  }

  def autobind(): Unit = {
    //superclass first
    classMappings.keys.toList.sortWith { (a, b) => a.isAssignableFrom(b) } foreach (cls => merge(classMappings(cls)))
  }

  /**
   * support features
   * <li> buildin primary type will be not null
   */
  def merge(entity: EntityTypeMapping): Unit = {
    val cls = entity.clazz
    // search parent and interfaces
    var supclz: Class[_] = cls.getSuperclass
    val supers = new mutable.ListBuffer[EntityTypeMapping]
    cls.getInterfaces foreach (i => if (classMappings.contains(i)) supers += classMappings(i))
    while (supclz != null && supclz != classOf[Object]) {
      if (classMappings.contains(supclz)) supers += classMappings(supclz)
      supclz.getInterfaces foreach (i => if (classMappings.contains(i)) supers += classMappings(i))
      supclz = supclz.getSuperclass
    }

    val inheris = Collections.newMap[String, PropertyMapping[_]]
    supers.reverse foreach { e =>
      inheris ++= e.properties
      if (entity.idGenerator == null) entity.idGenerator = e.idGenerator
      if (null == entity.cacheRegion && null == entity.cacheUsage) entity.cache(e.cacheRegion, e.cacheUsage)
    }

    val inherited = Collections.newMap[String, PropertyMapping[_]]
    entity.properties foreach {
      case (name, p) =>
        if (p.mergeable && inheris.contains(name)) inherited.put(name, inheris(name).copy())
    }
    entity.properties ++= inherited
  }

  def autobind(cls: Class[_], entityName: String, typ: ru.Type): EntityTypeMapping = {
    val fixedEntityName = if (entityName == null) Jpas.findEntityName(cls) else entityName
    val entity = refEntity(cls, fixedEntityName)
    val mapping = refMapping(cls, fixedEntityName)
    val mh = Mappings.Holder(mapping, entity)
    if (cls.isAnnotationPresent(Jpas.JpaEntityAnn)) return mapping
    val manifest = BeanInfos.get(mapping.clazz, typ)
    manifest.readables foreach {
      case (name, prop) =>
        if (prop.readable & prop.writable) {

          val optional = prop.typeinfo.optional
          val propType = prop.typeinfo.clazz
          val p =
            if (name == "id") {
              bindId(mh, name, propType, typ)
            } else if (isEntity(propType)) {
              bindManyToOne(mh, name, propType, optional)
            } else if (isSeq(propType)) {
              bindSeq(mh, name, propType, typ)
            } else if (isSet(propType)) {
              bindSet(mh, name, propType, typ)
            } else if (isMap(propType)) {
              bindMap(mh, name, propType, typ)
            } else if (isComponent(propType)) {
              bindComponent(mh, name, propType, typ)
            } else {
              bindScalar(mh, name, propType, scalarTypeName(name, propType), optional)
            }

          mapping.properties += (name -> p)
        }
    }
    mapping
  }

  def buildDomain(): Domain = {
    new ImmutableDomain(entities.toMap)
  }

  private def refEntity(clazz: Class[_], entityName: String): EntityTypeImpl = {
    entities.get(entityName) match {
      case Some(entity) => entity
      case None =>
        val e = new EntityTypeImpl(clazz, entityName)
        entities.put(entityName, e)
        e
    }
  }

  private def refMapping(clazz: Class[_], entityName: String): EntityTypeMapping = {
    entityMappings.get(entityName) match {
      case Some(entity) => entity
      case None =>
        val naming = namingPolicy.classToTableName(clazz, entityName)
        val schema = database.getOrCreateSchema(naming.schema.get)
        val table = schema.createTable(naming.text)
        val e = new EntityTypeMapping(refEntity(clazz, entityName), table)
        entityMappings.put(entityName, e)
        e
    }
  }

  private def bindComponent(mh: Mappings.Holder, name: String, propertyType: Class[_], tpe: ru.Type): SingularMapping = {
    val ct = new EmbeddableTypeImpl(propertyType)
    val cp = new SingularPropertyImpl(name, propertyType, ct)
    mh.meta.addProperty(cp)
    val cem = new EmbeddableTypeMapping(ct)
    val cpm = new SingularMapping(cp, cem)
    val ctpe = tpe.member(ru.TermName(name)).asMethod.returnType
    val manifest = BeanInfos.get(propertyType, ctpe)
    manifest.readables foreach {
      case (name, prop) =>
        if (prop.writable) {
          val optional = prop.typeinfo.optional
          val propType = prop.typeinfo.clazz
          val cmh = new Mappings.Holder(mh.mapping, ct)
          val p =
            if (isEntity(propType)) {
              if (propType == mh.mapping.clazz) {
                ct.parentName = Some(name); null.asInstanceOf[PropertyMapping[SingularProperty]]
              } else {
                bindManyToOne(cmh, name, propType, optional)
              }
            } else if (isSeq(propType)) {
              bindSeq(cmh, name, propType, ctpe)
            } else if (isSet(propType)) {
              bindSet(cmh, name, propType, ctpe)
            } else if (isMap(propType)) {
              bindMap(cmh, name, propType, ctpe)
            } else if (isComponent(propType)) {
              bindComponent(cmh, name, propType, ctpe)
            } else {
              bindScalar(cmh, name, propType, scalarTypeName(name, propType), optional)
            }
          if (null != p) cem.properties += (name -> p)
        }
    }
    cpm
  }

  private def scalarTypeName(name: String, clazz: Class[_]): String = {
    if (clazz.isAnnotationPresent(classOf[value])) {
      valueTypes += clazz
      clazz.getName
    } else if (classOf[Enumeration#Value].isAssignableFrom(clazz)) {
      val typeName = clazz.getName
      enumTypes.put(typeName, Strings.substringBeforeLast(typeName, "$"))
      typeName
    } else {
      if (-1 == clazz.getName.indexOf('.') || clazz.getName.startsWith("java.")) {
        Primitives.unwrap(clazz).getName
      } else {
        clazz.getName
      }
    }
  }

  private def bindMap(mh: Mappings.Holder, name: String, propertyType: Class[_], tye: ru.Type): MapMapping = {

    val typeSignature = typeNameOf(tye, name)
    val kvtype = Strings.substringBetween(typeSignature, "[", "]")

    var mapKeyType = Strings.substringBefore(kvtype, ",").trim
    var mapEleType = Strings.substringAfter(kvtype, ",").trim
    mapKeyType = if (mapKeyType.contains(".")) mapKeyType else "java.lang." + mapKeyType
    mapEleType = if (mapEleType.contains(".")) mapEleType else "java.lang." + mapEleType

    var keyMeta: Type = null
    var keyMapping: Mapping = null

    var eleMeta: Type = null
    var eleMapping: Mapping = null

    val mapKeyClazz = ClassLoaders.load(mapKeyType)
    if (isEntity(mapKeyClazz)) {
      val k = refEntity(mapKeyClazz, mapKeyType)
      keyMeta = k
      val idType = BeanInfos.get(mapKeyClazz).getPropertyType("id").get
      keyMapping = new BasicTypeMapping(new BasicType(idType), refColumn(mapKeyClazz, mapKeyType))
    } else {
      val k = new BasicType(mapKeyClazz)
      keyMeta = k
      keyMapping = new BasicTypeMapping(k, newColumn("name", mapKeyClazz, false))
    }

    val mapEleClazz = ClassLoaders.load(mapEleType)
    if (isEntity(mapEleClazz)) {
      val e = refEntity(mapEleClazz, mapEleType)
      eleMeta = e
      val idType = BeanInfos.get(mapEleClazz).getPropertyType("id").get
      eleMapping = new BasicTypeMapping(new BasicType(idType), refColumn(mapEleClazz, mapEleType))
    } else {
      val e = new BasicType(mapEleClazz)
      eleMeta = e
      eleMapping = new BasicTypeMapping(e, newColumn("value", mapEleClazz, false))
    }

    val meta = new MapPropertyImpl(name, propertyType, keyMeta, eleMeta)
    mh.meta.addProperty(meta)
    val p = new MapMapping(meta, keyMapping, eleMapping)
    if (propertyType.getName.startsWith("scala.")) p.typeName = Some("map")
    p
  }

  private def typeNameOf(tye: ru.Type, name: String): String = {
    tye.member(ru.TermName(name)).typeSignatureIn(tye).toString()
  }

  private def bindSeq(mh: Mappings.Holder, name: String, propertyType: Class[_], tye: ru.Type): CollectionMapping = {
    val typeSignature = typeNameOf(tye, name)
    val entityName = Strings.substringBetween(typeSignature, "[", "]")
    val entityClazz = ClassLoaders.load(entityName)
    val toManyElement = refEntity(entityClazz, entityName)
    val meta = new CollectionPropertyImpl(name, propertyType, toManyElement)
    mh.meta.addProperty(meta)

    val p = new CollectionMapping(meta, refMapping(entityClazz, entityName))
    p.ownerColumn = refColumn(entityClazz, entityName)
    //    if (propertyType.getName.startsWith("scala.")) p.typeName = Some("seq")
    p
  }

  private def bindSet(mh: Mappings.Holder, name: String, propertyType: Class[_], tye: ru.Type): CollectionMapping = {
    val typeSignature = typeNameOf(tye, name)
    val entityName = Strings.substringBetween(typeSignature, "[", "]")
    val entityClazz = ClassLoaders.load(entityName)
    val toManyElement = refEntity(entityClazz, entityName)
    val meta = new CollectionPropertyImpl(name, propertyType, toManyElement)
    mh.meta.addProperty(meta)

    val p = new CollectionMapping(meta, refMapping(entityClazz, entityName))
    p.ownerColumn = refColumn(entityClazz, entityName)
    //    if (propertyType.getName.startsWith("scala.")) p.typeName = Some("set")
    p
  }

  private def bindId(mh: Mappings.Holder, name: String, propertyType: Class[_], tye: ru.Type): SingularMapping = {
    val typ = new BasicType(propertyType)
    val meta = new SingularPropertyImpl(name, propertyType, typ)
    meta.optional = false
    mh.meta.addProperty(meta)

    val column = newColumn(columnName(name), propertyType, false)
    column.nullable = meta.optional
    val elemMapping = new BasicTypeMapping(typ, column)

    val p = new SingularMapping(meta, elemMapping)
    mh.mapping.table.add(column)

    p
  }

  private def bindScalar(mh: Mappings.Holder, name: String, propertyType: Class[_], typeName: String, optional: Boolean): SingularMapping = {
    val typ = new BasicType(propertyType)
    val meta = new SingularPropertyImpl(name, propertyType, typ)
    meta.optional = optional
    mh.meta.addProperty(meta)

    val column = newColumn(columnName(name, false), propertyType, true)
    column.nullable = meta.optional
    val elemMapping = new BasicTypeMapping(typ, column)
    val p = new SingularMapping(meta, elemMapping)
    if (None == p.typeName) p.typeName = Some(typeName)

    mh.mapping.table.add(column)
    p
  }

  private def bindManyToOne(mh: Mappings.Holder, name: String, propertyType: Class[_], optional: Boolean): SingularMapping = {
    val typ = refEntity(propertyType, propertyType.getName)
    val meta = new SingularPropertyImpl(name, propertyType, typ)
    meta.optional = optional
    mh.meta.addProperty(meta)

    val column = newColumn(columnName(name, true), propertyType, optional)
    val idType = BeanInfos.get(propertyType).getPropertyType("id").get
    val p = new SingularMapping(meta, new BasicTypeMapping(new BasicType(idType), column))
    mh.mapping.table.add(column)

    p
  }

  private def newColumn(name: String, clazz: Class[_], optional: Boolean): Column = {
    new Column(database.engine.toIdentifier(name), this.sqlTypeMapping.sqlType(clazz), optional)
  }
  def refColumn(clazz: Class[_], entityName: String): Column = {
    val idType = BeanInfos.get(clazz).getPropertyType("id").get
    new Column(database.engine.toIdentifier(columnName(entityName, true)), sqlTypeMapping.sqlType(idType), false)
  }
}
