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

import org.beangle.commons.bean.meta.MetaModel.ClassMeta

import scala.quoted.*

/** Compile-time ClassMeta digger entry — mirror of BeanInfos.of.
  *
  * Uses the same static-compilation logic as BeanInfoDigger (fields, accessors,
  * constructors, defaults resolved at compile time) to produce the
  * reflection-free [[ClassMeta]] directly.
  */
object ClassMetas {

  /** Digs ClassMeta for classes (macro, compile-time). */
  inline def of(inline clazzes: Class[_]*): List[ClassMeta] = ${ ClassMetaDigger.digInto('clazzes) }

  /** Digs ClassMeta for single class (macro, compile-time). */
  inline def of[T](clazz: Class[T]): ClassMeta = ${ ClassMetaDigger.digInto('clazz) }
}
