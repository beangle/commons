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
  //  def isAssociation:Boolean
  //  def isCollection:Boolean
  //  def isMap:Boolean
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
