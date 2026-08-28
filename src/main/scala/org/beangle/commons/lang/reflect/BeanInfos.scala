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

import org.beangle.commons.bean.meta.MetaModel.BeanMeta
import org.beangle.commons.bean.meta.{MetaLoader, MetaModel, MetaModels}
import org.beangle.commons.collection.IdentityCache

/** Global BeanInfo cache. Runtime entry point; compile-time dig goes through [[MetaModels.of]]. */
object BeanInfos {

  private val cache = new IdentityCache[Class[_], BeanInfo]

  /** Gets BeanInfo from cache. On miss, tries MetaModels (binary) then MetaLoader (reflection). */
  def get(clazz: Class[_]): BeanInfo = {
    val exist = cache.get(clazz)
    if (null != exist) return exist
    MetaModels.get(clazz) match
      case Some(meta) => register(meta)
      case None =>
        val parent = parentOf(clazz)
        if (null == parent) register(MetaLoader.load(clazz))
        else
          MetaModels.get(parent) match
            case Some(pm) => register(pm.copy(clazz = clazz, ctors = Seq.empty))
            case None => register(MetaLoader.load(clazz))
  }

  /** 父类 `$` 子类的父类判定（如 Hibernate 懒加载代理 `<Entity>$HibernateProxy`）：
   * 类名中 `$` 前缀与父类全名一致时返回父类。命中时复用父类 BeanMeta（此类无自有
   * bean 属性，仅继承父类）；native 下需该类已注册 allPublicMethods（构建期生成器
   * 按命名约定输出），供 `BeanInfo.from` 的 `getMethods` 查询。
   */
  private def parentOf(clazz: Class[_]): Class[_] = {
    val name = clazz.getName
    val idx = name.lastIndexOf("$")
    if (idx <= 0) null
    else {
      val parent = clazz.getSuperclass
      if (null != parent && name.substring(0, idx) == parent.getName) parent else null
    }
  }

  /** Returns true if BeanInfo is cached for the class. */
  def cached(clazz: Class[_]): Boolean = cache.contains(clazz)

  /** Registers a pre-built BeanInfo into the cache. */
  def update(bi: BeanInfo): BeanInfo = {
    cache.put(bi.meta.clazz, bi)
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
