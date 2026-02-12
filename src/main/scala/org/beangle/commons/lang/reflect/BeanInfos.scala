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

package org.beangle.commons.lang.reflect

import org.beangle.commons.collection.IdentityCache
import org.beangle.commons.lang.reflect.Reflections.deduceParamTypes

import java.lang.reflect.{Method, Modifier, ParameterizedType, TypeVariable}
import scala.collection.immutable.ArraySeq
import scala.collection.mutable
import scala.quoted.*

/** Global BeanInfo cache and of() macros. */
object BeanInfos {

  /** Shared BeanInfo cache. */
  val cache = new BeanInfoCache

  /** Gets BeanInfo from cache or loads by reflection. */
  def get(clazz: Class[_]): BeanInfo = cache.get(clazz)

  /** Digs BeanInfo for classes (macro, compile-time). */
  inline def of(inline clazzes: Class[_]*): List[BeanInfo] = ${ BeanInfoDigger.digInto('clazzes, 'cache) }

  /** Digs BeanInfo for single class (macro, compile-time). */
  inline def of[T](clazz: Class[T]): BeanInfo = ${ BeanInfoDigger.digInto('clazz, 'cache) ; }

  /** Returns true if BeanInfo is cached for the class. */
  def cached(clazz: Class[_]): Boolean = cache.contains(clazz)
}
