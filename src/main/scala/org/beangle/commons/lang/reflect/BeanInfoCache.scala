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

import org.beangle.commons.bean.meta.MetaLoader
import org.beangle.commons.collection.IdentityCache

/** Cache for BeanInfo by class. */
class BeanInfoCache {

  private val cache = new IdentityCache[Class[_], BeanInfo]

  /** Registers BeanInfo. */
  def update(bi: BeanInfo): BeanInfo = {
    cache.put(bi.meta.clazz, bi)
    bi
  }

  /** Registers BeanInfo for a subclass (clazz must extend bi.meta.clazz). */
  def update(clazz: Class[_], bi: BeanInfo): BeanInfo = {
    require(bi.meta.clazz.isAssignableFrom(clazz), s"${clazz.getName} is not a subclass of ${bi.meta.clazz.getName}")
    cache.put(clazz, bi)
    bi
  }

  /** Loads BeanInfo via MetaLoader (ClassMeta) + BeanInfo.from reconstruction. */
  def get(clazz: Class[_]): BeanInfo = {
    val exist = cache.get(clazz)
    if (null != exist) return exist
    val cm = MetaLoader.load(clazz)
    val bi = BeanInfo.from(cm)
    cache.put(clazz, bi)
    bi
  }

  /** Returns true if BeanInfo is cached for the class. */
  def contains(clazz: Class[_]): Boolean = cache.contains(clazz)

  /** Clears all cached BeanInfo. */
  def clear(): Unit = cache.clear()

  /** Number of cached BeanInfo entries. */
  def size: Int = cache.size()
}
