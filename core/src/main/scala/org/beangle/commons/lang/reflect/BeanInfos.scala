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

import java.lang.reflect.{Method, Modifier, ParameterizedType, TypeVariable}
import org.beangle.commons.collection.IdentityCache
import org.beangle.commons.lang.reflect.Reflections.deduceParamTypes

import scala.collection.mutable
import scala.collection.immutable.ArraySeq
import scala.quoted.*

object BeanInfos {

  val cache = new BeanInfoCache

  /**
    * Get ClassInfo from cache or load it by type.
    */
  def get(clazz: Class[_]): BeanInfo = cache.get(clazz)

  inline def of(inline clazzes:Class[_]*):List[BeanInfo] = ${BeanInfoDigger.digInto('clazzes,'cache)}

  inline def of[T](clazz:Class[T]): BeanInfo = ${ BeanInfoDigger.digInto('clazz,'cache);}

  def cached(clazz:Class[_]):Boolean = cache.contains(clazz)
}
