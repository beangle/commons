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

package org.beangle.commons.conversion.impl

import org.beangle.commons.conversion.Conversion
import org.beangle.commons.conversion.converter.{CtorConverter, MethodConverter}
import org.beangle.commons.lang.{Companions, Objects, Primitives}

import java.lang.reflect.Array
import scala.collection.{concurrent, mutable}
import scala.language.existentials

/** Converts values using a pre-built converter registry.
 *
 * Lookup walks source/target type hierarchies; ctor and companion `apply` converters
 * are discovered on demand and cached locally (not registered into the registry).
 *
 * @author chaostone
 * @since 3.2.0
 */
class GenericConversion(private val converters: Map[Class[_], Map[Class[_], GenericConverter]]) extends Conversion {

  private val cache = new concurrent.TrieMap[(Class[_], Class[_]), GenericConverter]

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
      if null == converter then Array.newInstance(targetObjClazz, 0).asInstanceOf[T]
      else
        val length = Array.getLength(source)
        val result = Array.newInstance(targetObjClazz, length).asInstanceOf[T]
        for (i <- 0 until length) Array.set(result, i, converter.convert(Array.get(source, i), targetObjType))
        result
    } else {
      val converter = findConverter(sourceClazz, targetClazz)
      val rs = converter.convert(source, targetClazz)
      if (null == rs && target.isPrimitive) Objects.default(target) else rs
    }
  }

  protected def findConverter(sourceType: Class[_], targetType: Class[_]): GenericConverter = {
    val key = (sourceType, targetType)
    cache.getOrElseUpdate(key, resolveConverter(sourceType, targetType))
  }

  private def resolveConverter(sourceType: Class[_], targetType: Class[_]): GenericConverter = {
    var converter = searchConverter(sourceType, targetType)
    if (null == converter) converter = findCtorConverter(sourceType, targetType)
    if (null == converter) NoneConverter else converter
  }

  /** Creates a converter from target's constructor or companion `apply` (cached only). */
  private def findCtorConverter(sourceType: Class[_], targetType: Class[_]): GenericConverter = {
    val ctor = targetType.getConstructors.find { c =>
      val pts = c.getParameterTypes
      pts.length == 1 && pts.contains(sourceType)
    }
    ctor match
      case None =>
        Companions.getCompanion(targetType) match {
          case None => null
          case Some(ct) =>
            try {
              new MethodConverter(sourceType, targetType, ct, ct.getClass.getDeclaredMethod("apply", sourceType))
            } catch
              case e: Exception => null //ignore
        }
      case Some(ct) => new CtorConverter(sourceType, targetType, ct)
  }

  protected def searchConverter(sourceType: Class[_], targetType: Class[_]): GenericConverter = {
    val interfaces = new mutable.LinkedHashSet[Class[_]]
    val classQueue = new mutable.Queue[Class[_]]
    classQueue += sourceType
    while (classQueue.nonEmpty) {
      val currentClass = classQueue.dequeue()
      val converter = getConverter(targetType, getConverters(currentClass))
      if (converter != null) return converter
      val superClass = currentClass.getSuperclass
      if (superClass != null && superClass != classOf[AnyRef]) classQueue += superClass
      for (interfaceType <- currentClass.getInterfaces) addInterfaces(interfaceType, interfaces)
    }
    val iter = interfaces.iterator
    while (iter.hasNext) {
      val interfaceType = iter.next()
      val converter = getConverter(targetType, getConverters(interfaceType))
      if (converter != null) return converter
    }
    getConverter(targetType, getConverters(classOf[AnyRef]))
  }

  private def getConverters(sourceType: Class[_]) = converters.getOrElse(sourceType, Map.empty)

  private def getConverter(targetType: Class[_], converters: Map[Class[_], GenericConverter]): GenericConverter = {
    val interfaces = new mutable.LinkedHashSet[Class[_]]
    val queue = new mutable.Queue[Class[_]]()
    queue += targetType
    while (queue.nonEmpty) {
      val cur = queue.dequeue()
      val converter = converters.get(cur).orNull
      if (converter != null) return converter
      val superClass = cur.getSuperclass
      if (superClass != null && superClass != classOf[AnyRef]) queue += superClass
      for (interfaceType <- cur.getInterfaces) addInterfaces(interfaceType, interfaces)
    }
    val iter = interfaces.iterator
    while (iter.hasNext) {
      val interfaceType = iter.next()
      val converter = converters.get(interfaceType).orNull
      if (converter != null) return converter
    }
    null
  }

  private def addInterfaces(interfaceType: Class[_], interfaces: mutable.Set[Class[_]]): Unit = {
    interfaces.add(interfaceType)
    for (inheritedInterface <- interfaceType.getInterfaces) addInterfaces(inheritedInterface, interfaces)
  }
}
