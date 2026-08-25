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

import org.beangle.commons.aot.{AotHintRegistrar, AotHints}
import org.beangle.commons.bean.meta.MetaModel.BeanMeta
import org.beangle.commons.collection.Collections

import java.io.OutputStream
import scala.collection.mutable
import scala.quoted.*

/** Class metadata registry: subclasses call [[register]] to register classes;
 * [[encode]] writes collected BeanMeta to the caller-specified stream (beanmeta.idx).
 *
 * {{{
 * class AppRegistry extends MetaRegistrar {
 *   register(classOf[User], classOf[Role])
 * }
 * val out = new FileOutputStream("beanmeta.idx")
 * new AppRegistry().encode(out)
 * val hints: AotHints = new AppRegistry().aotHints
 * }}}
 *
 * [[register]] is an inline macro: at the subclass call site (compile-time literal)
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

  /** Adds BeanMeta to internal buffer (used by macro expansion). */
  private def addMetas(cms: Iterable[BeanMeta]): Unit = {
    cms foreach { c => metaMap.put(c.clazz, c) }
  }

  /** Bridges registered metamodel classes into [[hints]] for GraalVM config generation. */
  override def registering(): Unit = {
    metaMap.keys.foreach(c => hints.registerType(c))
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
