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

import org.beangle.commons.bean.meta.MetaModel.BeanMeta

import java.io.OutputStream
import scala.collection.mutable
import scala.quoted.*

/** Class metadata registry: subclasses override [[registering]] template method
  * and call [[register]] to register classes; [[encode]] writes collected BeanMeta
  * to the caller-specified stream (metamodel.idx).
  *
  * {{{
  * class AppRegistry extends MetaRegistry {
  *   override def registering(): Unit = {
  *     register(classOf[User], classOf[Role])
  *   }
  * }
  * val out = new FileOutputStream("metamodel.idx")
  * new AppRegistry().encode(out)
  * }}}
  *
  * [[register]] is an inline macro: at the subclass call site (compile-time literal)
  * it invokes MetaDigger to dig BeanMeta, preserving generic precision.
  */
abstract class MetaRegistry {

  /** Registered class metadata. */
  private val metas = new mutable.ArrayBuffer[BeanMeta]

  private var registered = false

  /** Template method: subclasses override to call [[register]]. */
  protected def registering(): Unit = ()

  /** Registers classes at compile time (macro: digs BeanMeta, preserves precision). */
  protected inline def register(inline clazzes: Class[_]*): Unit = ${ MetaRegistry.registerImpl('clazzes, 'this) }

  /** Collects all registered class metadata (first call triggers registering). */
  def collect(): Seq[BeanMeta] = {
    if !registered then
      registering()
      registered = true
    metas.toSeq
  }

  /** Encodes collected class metadata to the specified stream (metamodel.idx). */
  def encode(out: OutputStream): Unit = MetaIndex.write(out, collect())

  /** Adds BeanMeta to internal buffer (used by macro expansion). */
  def addMetas(cms: Iterable[BeanMeta]): Unit = metas ++= cms
}

object MetaRegistry {

  /** Macro: digs class literal list and registers to registry. */
  def registerImpl(clazzes: Expr[Seq[Class[_]]], registry: Expr[MetaRegistry])(using Quotes): Expr[Unit] = {
    '{
      ${ registry }.addMetas(${ MetaDigger.digInto(clazzes) })
    }
  }
}
