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

import org.beangle.commons.bean.meta.{MetaLoader, MetaModel, MetaModels}
import org.beangle.commons.collection.IdentityCache

/** Global BeanInfo cache with compile-time macro and runtime reflection entry points. */
object BeanInfos {

  private val cache = new IdentityCache[Class[_], BeanInfo]

  /** Gets BeanInfo from cache. On miss, tries MetaModels (binary) then MetaLoader (reflection). */
  def get(clazz: Class[_]): BeanInfo = {
    val exist = cache.get(clazz)
    if (null != exist) return exist
    build(clazz, MetaModels.get(clazz).getOrElse(MetaLoader.load(clazz)))
  }

  /** Digs BeanInfo for classes. Binary lookup first, compile-time macro dig as fallback. */
  inline def of(inline clazzes: Class[_]*): List[BeanInfo] = {
    clazzes.toList.map(of)
  }

  /** Digs BeanInfo for single class. Binary lookup first, compile-time macro dig as fallback. */
  inline def of[T](clazz: Class[T]): BeanInfo = {
    build(clazz, MetaModels.get(clazz).getOrElse(MetaModels.of(clazz)))
  }

  /** Returns true if BeanInfo is cached for the class. */
  def cached(clazz: Class[_]): Boolean = cache.contains(clazz)

  def build(clazz: Class[_], cm: MetaModel.ClassMeta): BeanInfo = {
    val bi = BeanInfo.from(cm)
    cache.put(clazz, bi)
    bi
  }
}
