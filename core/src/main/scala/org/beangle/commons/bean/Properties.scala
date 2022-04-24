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

package org.beangle.commons.bean

import org.beangle.commons.conversion.Conversion
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.reflect.{BeanInfos, TypeInfo}
import org.beangle.commons.logging.Logging

import java.lang.reflect.Array as Jarray

object Properties {
  private val Default = new Properties(DefaultConversion.Instance)

  def set(bean: AnyRef, propertyName: String, value: Any): Any =
    Default.set(bean, propertyName, value)

  def get[T <: Any](inputBean: Any, propertyName: String): T =
    Default.get(inputBean, propertyName)

  def copy(bean: AnyRef, propertyName: String, value: Any): Any =
    Default.copy(bean, propertyName, value)

  def isWriteable(bean: AnyRef, name: String): Boolean =
    Default.isWriteable(bean, name)

  def getType(clazz: Class[_], name: String): Class[_] =
    Default.getType(clazz, name)

  def writables(clazz: Class[_]): Set[String] =
    Default.writables(clazz)
}

class Properties(conversion: Conversion) extends Logging {

  private val resolver = new PropertyNameResolver()

  @throws(classOf[NoSuchMethodException])
  def set(bean: AnyRef, propertyName: String, value: Any): Any =
    copy(bean, propertyName, value, null)

  def get[T <: Any](inputBean: Any, propertyName: String): T = {
    var result = inputBean
    var name = propertyName
    while (resolver.hasNested(name)) {
      val next = resolver.next(name)
      result =
        if (isMapType(result)) getPropertyOfMap(result, next)
        else if (resolver.isMapped(next)) getMappedProperty(result, next)
        else if (resolver.isIndexed(next)) getIndexedProperty(result, next)
        else getSimpleProperty(result, next)

      if (null != result && result.isInstanceOf[Option[_]])
        result = result.asInstanceOf[Option[_]].orNull
      if (null == result) return null.asInstanceOf[T]
      name = resolver.remove(name)
    }
    result =
      if (isMapType(result))
        getPropertyOfMap(result, name)
      else if (resolver.isMapped(name))
        getMappedProperty(result, name)
      else if (resolver.isIndexed(name))
        getIndexedProperty(result, name)
      else
        getSimpleProperty(result, name)
    result.asInstanceOf[T]
  }

  def copy(bean: AnyRef, propertyName: String, value: Any): Any =
    copy(bean, propertyName, value, this.conversion)

  def isWriteable(bean: AnyRef, name: String): Boolean =
    BeanInfos.get(bean.getClass).getSetter(name).isDefined

  def getType(clazz: Class[_], name: String): Class[_] =
    BeanInfos.get(clazz).getPropertyType(name).orNull

  def writables(clazz: Class[_]): Set[String] =
    BeanInfos.get(clazz).writables.keySet

  private def copy(bean: AnyRef, propertyName: String, value: Any, conversion: Conversion): Any = {
    var result: Any = bean
    var name = propertyName
    while (resolver.hasNested(name)) {
      result = getDirectProperty(result, resolver.next(name))
      if (null != result && result.isInstanceOf[Option[_]])
        result = result.asInstanceOf[Option[_]].orNull
      if (result == null) throw new RuntimeException("Null property value for '" + name + "' on bean class '" + bean.getClass + "'")
      name = resolver.remove(name)
    }

    if (isMapType(result)) {
      setPropertyOfMapBean(result, name, value)
      value
    } else if (resolver.isMapped(name)) {
      copyMappedProperty(result, name, value)
      value
    } else if (resolver.isIndexed(name))
      copyIndexedProperty(result, name, value, conversion)
    else
      copySimpleProperty(result, name, value, conversion)
  }

  private def getDirectProperty(result: Any, name: String): Any =
    if (isMapType(result)) getPropertyOfMap(result, name)
    else if (resolver.isMapped(name)) getMappedProperty(result, name)
    else if (resolver.isIndexed(name)) getIndexedProperty(result, name)
    else getSimpleProperty(result, name)

  private def getSimpleProperty(bean: Any, name: String): Any =
    BeanInfos.get(bean.getClass).getGetter(name) match {
      case Some(method) => method.invoke(bean)
      case _ =>
        logger.error("Cannot find " + Strings.capitalize(name) + " Getter in " + bean.getClass)
        null
    }

  private def getPropertyOfMap(bean: Any, propertyName: String): Any = {
    var name = resolver.getProperty(propertyName)
    if name == null || name.isEmpty then name = resolver.getKey(propertyName)
    else name = propertyName
    getMapped(bean, name)
  }

  private def getMappedProperty(bean: Any, name: String): Any = {
    val key = resolver.getKey(name)
    if (key == null)
      throw new IllegalArgumentException("Invalid mapped property '" + name + "'")
    val value = getSimpleProperty(bean, resolver.getProperty(name))
    if (null == value) null else getMapped(value, key)
  }

  private def getIndexedProperty(bean: Any, name: String): Any = {
    val index = resolver.getIndex(name)
    if (index < 0)
      throw new IllegalArgumentException("Invalid indexed property '" + name + "'")
    val value = getSimpleProperty(bean, resolver.getProperty(name))
    if (null == value) return null
    if (value.getClass.isArray) Jarray.get(value, index) else getIndexed(value, index)
  }

  private def copySimpleProperty(bean: Any, name: String, value: Any, conversion: Conversion): Any = {
    val manifest = BeanInfos.get(bean.getClass)
    manifest.getSetter(name) match {
      case Some(method) =>
        val p = manifest.properties(name)
        val converted = convert(value, p.clazz, p.typeinfo, conversion)
        method.invoke(bean, converted.asInstanceOf[Object])
        converted
      case _ => null
    }
  }

  private def copyIndexedProperty(bean: Any, name: String, value: Any, conversion: Conversion): Any = {
    var index = -1
    try
      index = resolver.getIndex(name)
    catch {
      case e: IllegalArgumentException =>
        throw new IllegalArgumentException("Invalid indexed property '" + name + "' on bean class '" + bean.getClass + "'")
    }
    if (index < 0) throw new IllegalArgumentException("Invalid indexed property '" + name
      + "' on bean class '" + bean.getClass + "'")

    // Isolate the name
    val resolvedName = resolver.getProperty(name)
    val rs = if (resolvedName != null && resolvedName.length() >= 0) getSimpleProperty(bean, resolvedName) else bean

    var converted = value
    if (rs.getClass.isArray) {
      converted = convert(value, rs.getClass.getComponentType, null, conversion)
      Jarray.set(rs, index, value)
    } else
      setIndexed(rs, index, value)
    converted
  }

  private def getMappedKey(name: String, bean: Any): String = {
    var key: String = null
    try
      key = resolver.getKey(name)
    catch {
      case e: IllegalArgumentException =>
        throw new IllegalArgumentException("Invalid mapped property '" + name + "' on bean class '" + bean.getClass + "'")
    }
    if (key == null) throw new IllegalArgumentException("Invalid mapped property '" + name
      + "' on bean class '" + bean.getClass + "'")
    key
  }

  private def convert(value: Any, clazz: Class[_], typeInfo: TypeInfo, conversion: Conversion): Any =
    if (typeInfo.isOptional) {
      if null == value then None
      else if value.isInstanceOf[Option[_]] then value
      else if null != typeInfo then
        if (null == conversion) Option(value) else Option(conversion.convert(value, typeInfo.args.head.clazz))
      else Option(value)
    } else if (null == conversion) {
      value
    } else {
      conversion.convert(value, clazz)
    }

  private def copyMappedProperty(bean: Any, name: String, value: Any): Unit = {
    val key = getMappedKey(name, bean)
    val resolvedName = resolver.getProperty(name)
    val rs = if (resolvedName != null && resolvedName.length() >= 0) getSimpleProperty(bean, resolvedName) else bean
    val typeInfo = BeanInfos.get(bean.getClass).getPropertyTypeInfo(resolvedName).get
    val key1 = conversion.convert(key, typeInfo.args.head.clazz)
    val value1 = conversion.convert(value, typeInfo.args.tail.head.clazz)
    setMapped(rs, key1, value1)
  }

  private def setPropertyOfMapBean(bean: Any, propertyName: String, value: Any): Unit = {
    var pname = propertyName
    if (resolver.isMapped(propertyName)) {
      val name = resolver.getProperty(propertyName)
      if (name == null || name.length() == 0)
        pname = resolver.getKey(propertyName)
    }
    if (resolver.isIndexed(pname) || resolver.isMapped(pname)) throw new IllegalArgumentException(
      "Indexed or mapped properties are not supported on" + " objects of type Map: " + pname)
    setMapped(bean, pname, value)
  }

  private def getIndexed(bean: Any, index: Int): Any =
    bean match {
      case null => null
      case s: collection.Seq[_] => s(index)
      case x: java.util.List[_] => x.get(index)
      case _ => throw new RuntimeException("Don't support getIndexed on " + bean.getClass)
    }

  private def setIndexed(bean: Any, index: Int, value: Any): Unit =
    bean match {
      case null =>
      case s: collection.mutable.Seq[_] => s.asInstanceOf[collection.mutable.Seq[Any]].update(index, value)
      case x: java.util.List[_] => x.asInstanceOf[java.util.List[Any]].set(index, value)
      case _ => throw new RuntimeException("Don't support setIndexed on " + bean.getClass)
    }

  private def setMapped(bean: Any, key: Any, value: Any): Unit =
    bean match {
      case null =>
      case s: collection.mutable.Map[_, _] => s.asInstanceOf[collection.mutable.Map[Any, Any]].put(key, value)
      case x: java.util.Map[_, _] => x.asInstanceOf[java.util.Map[Any, Any]].put(key, value)
      case _ => throw new RuntimeException("Don't support setMaped on " + bean.getClass)
    }

  private def getMapped(bean: Any, key: Any): Any =
    bean match {
      case null => null
      case s: collection.mutable.Map[_, _] => s.asInstanceOf[collection.mutable.Map[Any, _]].get(key).orNull
      case x: java.util.Map[_, _] => x.get(key)
      case _ => throw new RuntimeException("Don't support getMapped on " + bean.getClass)
    }

  private def isMapType(obj: Any): Boolean =
    obj.isInstanceOf[collection.mutable.Map[_, _]] || obj.isInstanceOf[java.util.Map[_, _]]
}
