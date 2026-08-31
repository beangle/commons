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

package org.beangle.commons.bean.meta

import org.beangle.commons.aot.AotHintRegistrar
import org.beangle.commons.bean.component
import org.beangle.commons.bean.meta.MetaModel.BeanMeta
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.reflect.{BeanInfos, TypeInfo}

import java.io.OutputStream
import scala.quoted.*

/** Class metadata registry: subclasses call [[register]] to register classes;
 * [[encode]] writes collected BeanMeta to the caller-specified stream (beanmeta.idx).
 *
 * {{{
 * class AppRegistry extends MetaRegistrar {
 *   override def registering(): Unit = register(classOf[User], classOf[Role])
 * }
 * val out = new FileOutputStream("beanmeta.idx")
 * new AppRegistry().encode(out)
 * val hints: AotHints = new AppRegistry().aotHints
 * }}}
 *
 * Subclasses implement `org.beangle.commons.lang.Registrar.registering`
 * and call [[register]] there;
 * [[register]] is an inline macro: at the call site (compile-time literal)
 * it invokes MetaDigger to dig BeanMeta, preserving generic precision.
 */
abstract class MetaRegistrar extends AotHintRegistrar {

  /** Registered class metadata. */
  private val metaMap = Collections.newMap[Class[_], BeanMeta]

  /** Registers classes at compile time (macro: digs BeanMeta, preserves precision). */
  protected inline def register(inline clazzes: Class[_]*): Unit = ${ MetaRegistrar.registerImpl('clazzes, 'this) }

  /** Encodes collected class metadata to the specified stream (beanmeta.idx). */
  def encode(out: OutputStream): Unit = MetaIndex.write(out, metas)

  /** Returns all registered BeanMeta entries. */
  def metas: Iterable[BeanMeta] = metaMap.values

  /** Adds BeanMeta to internal buffer (used by macro expansion).
   * Dont change to protected or private due to substream library may invoke it by registerImpl macro*/
  def addMetas(cms: Iterable[BeanMeta]): Unit = {
    val visited = Collections.newSet[Class[_]]
    cms foreach { c =>
      metaMap.put(c.clazz, c)
      hints.registerType(c.clazz)
      registerEnumProperties(c, visited)
    }
  }

  /** 注册实体属性树中出现的 Scala 3 enum：遍历属性，遇到 `@component` 值类型递归其属性，
   *  集合/Map 深入元素类型。应用只需注册实体，枚举属性经 [[AotHints.registerEnum]]
   *  一并登记枚举类、伴生对象、全部值类与序列化，无需手工 `registerType(classOf[枚举])`。
   *  构建期调用（`addMetas` 仅经 `registering()` 由生成器触发），此处用反射 dig
   *  component 是安全的。 */
  private def registerEnumProperties(cm: BeanMeta, visited: scala.collection.mutable.Set[Class[_]]): Unit = {
    def dig(bm: BeanMeta): Unit = {
      if (visited.add(bm.clazz)) bm.properties foreach (p => visit(p.typeinfo))
    }
    def visit(ti: TypeInfo): Unit = {
      if (ti == null) return
      val clazz = ti.clazz
      if (classOf[scala.reflect.Enum].isAssignableFrom(clazz)) hints.registerEnum(clazz)
      else if (clazz.isAnnotationPresent(classOf[component])) dig(BeanInfos.get(clazz).meta)
      ti.args foreach visit
    }
    dig(cm)
  }
}

object MetaRegistrar {

  /** Macro: digs class literal list and registers to registry. */
  def registerImpl(clazzes: Expr[Seq[Class[_]]], registrar: Expr[MetaRegistrar])(using Quotes): Expr[Unit] = {
    '{
      ${ registrar }.addMetas(${ MetaDigger.digInto(clazzes) })
    }
  }
}
