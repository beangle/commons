/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.lang.conversion.impl

import java.lang.reflect.Array
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Collections
import java.util.HashSet
import java.util.LinkedHashSet
import java.util.LinkedList
import java.util.Map
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Primitives
import org.beangle.commons.lang.conversion.Conversion
import org.beangle.commons.lang.conversion.Converter
import org.beangle.commons.lang.conversion.ConverterRegistry
import org.beangle.commons.lang.tuple.Pair
import scala.collection.JavaConversions._
import org.beangle.commons.lang.Objects
import scala.language.existentials

/**
 * Generic Conversion Super class
 * It provider converter registry and converter search machanism.
 *
 * @author chaostone
 * @since 3.2.0
 */
abstract class AbstractGenericConversion extends Conversion with ConverterRegistry {

  var converters: Map[Class[_], Map[Class[_], GenericConverter]] = CollectUtils.newHashMap()

  var cache: Map[Pair[Class[_], Class[_]], GenericConverter] = CollectUtils.newConcurrentHashMap()

  protected def addConverter(converter: GenericConverter) {
    val key = converter.getTypeinfo
    getOrCreateConverters(key.getLeft.asInstanceOf[Class[_]])
      .put(key.getRight.asInstanceOf[Class[_]], converter)
    cache.clear()
  }

  override def addConverter(converter: Converter[_, _]) {
    var key: Pair[Class[_], Class[_]] = null
    val defaultKey = Pair.of(classOf[Any], classOf[Any])
    for (m <- converter.getClass.getMethods if m.getName == "apply" && Modifier.isPublic(m.getModifiers) && !m.isBridge()) {
      key = Pair.of[Class[_], Class[_]](m.getParameterTypes()(0), m.getReturnType)
    }
    if (null == key) throw new IllegalArgumentException("Cannot find convert type pair " + converter.getClass)
    getOrCreateConverters(key.getLeft.asInstanceOf[Class[_]])
      .put(key.getRight.asInstanceOf[Class[_]], new ConverterAdapter(converter, key))
    cache.clear()
  }

  private def getOrCreateConverters(sourceType: Class[_]): Map[Class[_], GenericConverter] = {
    var exists = converters.get(sourceType)
    if (null == exists) {
      exists = CollectUtils.newHashMap()
      converters.put(sourceType, exists)
    }
    exists
  }

  private def getConverters(sourceType: Class[_]): Map[Class[_], GenericConverter] = {
    val exists = converters.get(sourceType)
    if (null == exists) Collections.emptyMap() else exists
  }

  private def getConverter(targetType: Class[_], converters: Map[Class[_], GenericConverter]): GenericConverter = {
    val interfaces = new LinkedHashSet[Class[_]]()
    val queue = new LinkedList[Class[_]]()
    queue.addFirst(targetType)
    while (!queue.isEmpty) {
      val cur = queue.removeLast()
      val converter = converters.get(cur)
      if (converter != null) return converter
      val superClass = cur.getSuperclass
      if (superClass != null && superClass != classOf[AnyRef]) queue.addFirst(superClass)
      for (interfaceType <- cur.getInterfaces) addInterfaces(interfaceType, interfaces)
    }
    for (interfaceType <- interfaces) {
      val converter = converters.get(interfaceType)
      if (converter != null) return converter
    }
    null
  }

  private def addInterfaces(interfaceType: Class[_], interfaces: Set[Class[_]]) {
    interfaces.add(interfaceType)
    for (inheritedInterface <- interfaceType.getInterfaces) addInterfaces(inheritedInterface, interfaces)
  }

  protected def findConverter(sourceType: Class[_], targetType: Class[_]): GenericConverter = {
    val key = Pair.of(sourceType, targetType)
    var converter = cache.get(key)
    if (null == converter) converter = searchConverter(sourceType, targetType) else return converter
    if (null == converter) converter = NoneConverter else cache.put(key.asInstanceOf[Pair[Class[_], Class[_]]], converter)
    converter
  }

  protected def searchConverter(sourceType: Class[_], targetType: Class[_]): GenericConverter = {
    val interfaces = new LinkedHashSet[Class[_]]()
    val classQueue = new LinkedList[Class[_]]()
    classQueue.addFirst(sourceType)
    while (!classQueue.isEmpty) {
      val currentClass = classQueue.removeLast()
      val converter = getConverter(targetType, getConverters(currentClass))
      if (converter != null) return converter
      val superClass = currentClass.getSuperclass
      if (superClass != null && superClass != classOf[AnyRef]) classQueue.addFirst(superClass)
      for (interfaceType <- currentClass.getInterfaces) addInterfaces(interfaceType, interfaces)
    }
    for (interfaceType <- interfaces) {
      val converter = getConverter(targetType, getConverters(interfaceType))
      if (converter != null) return converter
    }
    getConverter(targetType, getConverters(classOf[AnyRef]))
  }

  /**
   * Convert to target type.
   */
  override def convert[T](source: Any, targetType: Class[T]): T = {
    if (null == source) return Objects.default(targetType)
    val sourceType = Primitives.wrap(source.getClass)
    val targetClazz = Primitives.wrap(targetType)
    if (targetClazz.isAssignableFrom(sourceType)) return source.asInstanceOf[T]
    if (sourceType.isArray && targetClazz.isArray) {
      val sourceObjType = Primitives.wrap(sourceType.getComponentType)
      val targetObjType = Primitives.wrap(targetClazz.getComponentType)
      val converter = findConverter(sourceObjType, targetObjType)
      if (null == converter) Array.newInstance(targetClazz.getComponentType, 0).asInstanceOf[T] else {
        val length = Array.getLength(source)
        val result = Array.newInstance(targetClazz.getComponentType, length).asInstanceOf[T]
        for (i <- 0 until length) Array.set(result, i, converter.convert(Array.get(source, i), sourceObjType,
          targetObjType))
        result
      }
    } else {
      val converter = findConverter(sourceType, targetClazz)
      if ((null == converter)) Objects.default(targetClazz) else converter.convert(source, sourceType, targetClazz).asInstanceOf[T]
    }
  }
}
