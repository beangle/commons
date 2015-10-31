/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.commons.conversion.impl

import java.lang.reflect.{ Array, Modifier }

import scala.collection.{ concurrent, mutable }
import scala.language.existentials

import org.beangle.commons.conversion.{ Conversion, Converter, ConverterRegistry }
import org.beangle.commons.lang.{ Objects, Primitives }
/**
 * Generic Conversion Super class
 * It provider converter registry and converter search machanism.
 *
 * @author chaostone
 * @since 3.2.0
 */
abstract class AbstractGenericConversion extends Conversion with ConverterRegistry {

  val converters = new mutable.HashMap[Class[_], Map[Class[_], GenericConverter]]

  val cache = new concurrent.TrieMap[Tuple2[Class[_], Class[_]], GenericConverter]
  /**
   * Convert to target type.
   */
  override def convert[T](source: Any, target: Class[T]): T = {
    if (null == source) return Objects.default(target)
    val sourceClazz = Primitives.wrap(source.getClass)
    val targetClazz = Primitives.wrap(target)

    if (targetClazz.isAssignableFrom(sourceClazz)) return source.asInstanceOf[T]

    if (sourceClazz.isArray && targetClazz.isArray) {
      val sourceObjType = Primitives.wrap(sourceClazz.getComponentType)
      val targetObjClazz = targetClazz.getComponentType
      val targetObjType = Primitives.wrap(targetObjClazz)
      val converter = findConverter(sourceObjType, targetObjType)
      if (null == converter) {
        Array.newInstance(targetObjClazz, 0).asInstanceOf[T]
      } else {
        val length = Array.getLength(source)
        val result = Array.newInstance(targetObjClazz, length).asInstanceOf[T]
        for (i <- 0 until length) Array.set(result, i, converter.convert(Array.get(source, i), targetObjType))
        result
      }
    } else {
      val converter = findConverter(sourceClazz, targetClazz)
      val rs = converter.convert(source, targetClazz).asInstanceOf[T]
      if (null == rs && target.isPrimitive) Objects.default(target) else rs
    }
  }

  override def addConverter(converter: Converter[_, _]) {
    var key: Tuple2[Class[_], Class[_]] = null
    val defaultKey = (classOf[Any], classOf[Any])
    for (m <- converter.getClass.getMethods if m.getName == "apply" && Modifier.isPublic(m.getModifiers) && !m.isBridge()) {
      key = (m.getParameterTypes()(0), m.getReturnType)
    }
    if (null == key) throw new IllegalArgumentException("Cannot find convert type pair " + converter.getClass)
    val sourceType = key._1.asInstanceOf[Class[_]]
    val adapter = new ConverterAdapter(converter, key)
    converters.get(sourceType) match {
      case Some(existed) => converters += (sourceType -> (existed + (key._2 -> adapter)))
      case _             => converters += (sourceType -> Map((key._2 -> adapter)))
    }
    cache.clear()
  }

  protected def addConverter(converter: GenericConverter) {
    val key = converter.getTypeinfo
    val sourceType = key._1.asInstanceOf[Class[_]]
    converters.get(sourceType) match {
      case Some(existed) =>
        converters += (key._1 -> (existed + (key._2 -> converter)))
      case _ => converters += (key._1 -> Map((key._2 -> converter)))
    }
    cache.clear()
  }

  protected def findConverter(sourceType: Class[_], targetType: Class[_]): GenericConverter = {
    val key = (sourceType, targetType)
    var converter = cache.get(key).orNull
    if (null == converter) converter = searchConverter(sourceType, targetType)
    if (null == converter) converter = NoneConverter
    else cache.put(key.asInstanceOf[Tuple2[Class[_], Class[_]]], converter)
    converter
  }

  protected def searchConverter(sourceType: Class[_], targetType: Class[_]): GenericConverter = {
    val interfaces = new mutable.LinkedHashSet[Class[_]]
    val classQueue = new mutable.Queue[Class[_]]
    classQueue += sourceType
    while (!classQueue.isEmpty) {
      val currentClass = classQueue.dequeue
      val converter = getConverter(targetType, getConverters(currentClass))
      if (converter != null) return converter
      val superClass = currentClass.getSuperclass
      if (superClass != null && superClass != classOf[AnyRef]) classQueue += superClass
      for (interfaceType <- currentClass.getInterfaces) addInterfaces(interfaceType, interfaces)
    }
    var iter = interfaces.iterator
    while (iter.hasNext) {
      val interfaceType = iter.next
      val converter = getConverter(targetType, getConverters(interfaceType))
      if (converter != null) return converter
    }
    getConverter(targetType, getConverters(classOf[AnyRef]))
  }

  private def getConverters(sourceType: Class[_]) = converters.get(sourceType).getOrElse(Map.empty)

  private def getConverter(targetType: Class[_], converters: Map[Class[_], GenericConverter]): GenericConverter = {
    val interfaces = new mutable.LinkedHashSet[Class[_]]
    val queue = new mutable.Queue[Class[_]]()
    queue += targetType
    while (!queue.isEmpty) {
      val cur = queue.dequeue()
      val converter = converters.get(cur).orNull
      if (converter != null) return converter
      val superClass = cur.getSuperclass
      if (superClass != null && superClass != classOf[AnyRef]) queue += superClass
      for (interfaceType <- cur.getInterfaces) addInterfaces(interfaceType, interfaces)
    }
    var iter = interfaces.iterator
    while (iter.hasNext) {
      val interfaceType = iter.next
      val converter = converters.get(interfaceType).orNull
      if (converter != null) return converter
    }
    null
  }

  private def addInterfaces(interfaceType: Class[_], interfaces: mutable.Set[Class[_]]) {
    interfaces.add(interfaceType)
    for (inheritedInterface <- interfaceType.getInterfaces) addInterfaces(inheritedInterface, interfaces)
  }
}
