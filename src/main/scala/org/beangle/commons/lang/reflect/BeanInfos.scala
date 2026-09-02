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

import java.lang.invoke.{MethodHandles, VarHandle}

/** Global BeanInfo cache. Runtime entry point; compile-time dig goes through [[MetaModels.of]]. */
object BeanInfos {

  /** 进程级 BeanInfo 缓存：volatile 字段 + VarHandle CAS。
   *  Class 不重写 equals/hashCode，键比较即引用相等（identity 语义）。
   *  读路径无锁（volatile load），写路径 CAS 重试，无 monitor（虚拟线程友好）。
   */
  @volatile private var cache: Map[Class[_], BeanInfo] = scala.collection.immutable.HashMap.empty

  private val CACHE: VarHandle =
    Invokers.findStaticVarHandle(MethodHandles.lookup(), classOf[BeanInfos.type], "cache", classOf[Map[Class[_], BeanInfo]])

  /** Gets BeanInfo from cache. On miss, tries MetaModels (binary) then MetaLoader (reflection).
   *  Throws for non-reflectable classes (see [[MetaLoader.supports]]). */
  def get(clazz: Class[_]): BeanInfo = {
    find(clazz) match
      case Some(bi) => bi
      case None => throw new RuntimeException("Cannot reflect class: " + clazz.getName)
  }

  /** Like [[get]], but returns None for non-reflectable classes instead of throwing. */
  def find(clazz: Class[_]): Option[BeanInfo] = {
    cache.get(clazz) match {
      case d@Some(bi) => d
      case None =>
        load(clazz) match {
          case d@Some(bi) =>
            put(clazz, bi)
            d
          case None => None
        }
    }
  }

  /** CAS 写：基于当前快照合并新条目，失败说明被并发修改则重试（写频率低，几乎不重试）。 */
  private def put(clazz: Class[_], bi: BeanInfo): Unit = {
    var done = false
    while (!done) {
      val old = cache
      done = CACHE.compareAndSet(old, old + (clazz -> bi))
    }
  }

  /** 从 MetaModels（二进制索引）或 MetaLoader（运行时反射）加载 BeanMeta 并构造 BeanInfo。 */
  private def load(clazz: Class[_]): Option[BeanInfo] = {
    MetaModels.get(clazz) match
      case Some(meta) => Some(BeanInfo.from(meta))
      case None =>
        // $ 子类/匿名类（如 Scala 3 枚举值类 NoticeStatus$$anon$1）复用父类（枚举本体）元数据
        val source = parentOf(clazz) match
          case null => clazz
          case parent => parent

        loadMeta(source).map { m =>
          val meta = if (m.clazz == clazz) m else m.copy(clazz = clazz, ctors = Seq.empty)
          BeanInfo.from(meta)
        }
  }

  /** 依次尝试二进制索引与运行时反射，返回来源类的 BeanMeta。 */
  private def loadMeta(source: Class[_]): Option[BeanMeta] =
    MetaModels.get(source).orElse(reflectMeta(source))

  /** 经 [[MetaModels.reflect]] 反射加载（native 下自动切换轻量 [[org.beangle.commons.bean.meta.MetaLoaderLite]]，
   * 仅支持可反射的应用类，见 [[MetaLoader.supports]]）。 */
  private def reflectMeta(clazz: Class[_]): Option[BeanMeta] =
    if MetaLoader.supports(clazz) then Some(MetaModels.reflect(clazz)) else None

  /** 定位 `$` 子类的父类，命中后复用父类 BeanMeta：
   *  - Hibernate 代理（`Entity$HibernateProxy`）：类名前缀与父类全名一致；
   *  - Scala 3 枚举值类（`NoticeStatus$$anon$1`）：父类可反射时直接取父类。
   * 这类子类没有自有属性，仅继承父类；native 下需已注册 allPublicMethods 供 getMethods 查询。
   */
  private def parentOf(clazz: Class[_]): Class[_] = {
    val name = clazz.getName
    val idx = name.lastIndexOf("$")
    if (idx <= 0) null
    else {
      val parent = clazz.getSuperclass
      if (null != parent &&
        (name.substring(0, idx) == parent.getName || (name.contains("$$") && MetaLoader.supports(parent)))) parent
      else null
    }
  }

  /** Returns true if BeanInfo is cached for the class. */
  def cached(clazz: Class[_]): Boolean = cache.contains(clazz)

  /** Registers a pre-built BeanInfo into the cache. */
  def update(bi: BeanInfo): BeanInfo = {
    put(bi.meta.clazz, bi)
    bi
  }

  /** Clears all cached BeanInfo. */
  def clear(): Unit = CACHE.set(scala.collection.immutable.HashMap.empty[Class[_], BeanInfo])

  /** Registers BeanInfo from BeanMeta into the cache. */
  def register(cm: MetaModel.BeanMeta): BeanInfo = {
    val bi = BeanInfo.from(cm)
    put(bi.clazz, bi)
    bi
  }
}
