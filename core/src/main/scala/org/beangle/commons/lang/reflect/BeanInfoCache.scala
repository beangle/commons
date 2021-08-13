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

class BeanInfoCache {

  /**
    * class info cache
    */
  private var cache = new IdentityCache[Class[_], BeanInfo]

  inline def of(inline clazzes: Class[_]*): List[BeanInfo] = ${BeanInfoDigger.digInto('clazzes, 'this)}

  inline def of[T](clazz: Class[T]): BeanInfo = ${BeanInfoDigger.digInto('clazz, 'this);}

  /** register classInfo
    * @param bi
    */
  def update(bi: BeanInfo): BeanInfo = {
    cache.put(bi.clazz, bi)
    bi
  }

  /**
    * Load ClassInfo using reflections
    */
  def get(clazz: Class[_]): BeanInfo = {
    var exist = cache.get(clazz)
    if (null != exist) return exist
    val ci = BeanInfoLoader.load(clazz)
    cache.put(clazz, ci)
    ci
  }

  def contains(clazz: Class[_]): Boolean = cache.contains(clazz)

  def clear(): Unit = cache.clear()

  def size: Int = cache.size()
}
