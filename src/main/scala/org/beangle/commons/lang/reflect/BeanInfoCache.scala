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

/** Cache for BeanInfo by class. */
class BeanInfoCache {

  private val cache = new IdentityCache[Class[_], BeanInfo]

  /** Digs BeanInfo for classes at compile-time (macro). */
  inline def of(inline clazzes: Class[_]*): List[BeanInfo] = ${ BeanInfoDigger.digInto('clazzes, 'this) }

  /** Digs BeanInfo for single class at compile-time (macro). */
  inline def of[T](clazz: Class[T]): BeanInfo = ${ BeanInfoDigger.digInto('clazz, 'this) ; }

  /** Registers BeanInfo. */
  def update(bi: BeanInfo): BeanInfo = {
    cache.put(bi.clazz, bi)
    bi
  }

  /** Registers BeanInfo for a subclass (clazz must extend bi.clazz). */
  def update(clazz: Class[_], bi: BeanInfo): BeanInfo = {
    require(bi.clazz.isAssignableFrom(clazz), s"${clazz.getName} is not a subclass of ${bi.clazz.getName}")
    cache.put(clazz, bi)
    bi
  }

  /** Loads BeanInfo via reflection. */
  def get(clazz: Class[_]): BeanInfo = {
    val exist = cache.get(clazz)
    if (null != exist) return exist
    val ci = BeanInfoLoader.load(clazz)
    cache.put(clazz, ci)
    ci
  }

  /** Returns true if BeanInfo is cached for the class. */
  def contains(clazz: Class[_]): Boolean = cache.contains(clazz)

  /** Clears all cached BeanInfo. */
  def clear(): Unit = cache.clear()

  /** Number of cached BeanInfo entries. */
  def size: Int = cache.size()
}
