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

/** Global BeanInfo cache. Runtime entry point; compile-time dig goes through [[MetaModels.of]]. */
object BeanInfos {

  private val cache = new IdentityCache[Class[_], BeanInfo]

  /** Gets BeanInfo from cache. On miss, tries MetaModels (binary) then MetaLoader (reflection). */
  def get(clazz: Class[_]): BeanInfo = {
    val exist = cache.get(clazz)
    if (null != exist) return exist
    register(MetaModels.get(clazz).getOrElse(MetaLoader.load(clazz)))
  }

  /** Returns true if BeanInfo is cached for the class. */
  def cached(clazz: Class[_]): Boolean = cache.contains(clazz)

  /** Registers a pre-built BeanInfo into the cache. */
  def update(bi: BeanInfo): BeanInfo = {
    cache.put(bi.meta.clazz, bi)
    bi
  }

  /** Registers a BeanInfo for a specific class (e.g. subclass sharing parent's BeanInfo). */
  def update(clazz: Class[_], bi: BeanInfo): BeanInfo = {
    cache.put(clazz, bi)
    bi
  }

  /** Clears all cached BeanInfo. */
  def clear(): Unit = cache.clear()

  /** Registers BeanInfo from BeanMeta into the cache. */
  def register(cm: MetaModel.BeanMeta): BeanInfo = {
    val bi = BeanInfo.from(cm)
    cache.put(bi.clazz, bi)
    bi
  }
}
