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
import org.beangle.commons.io.Resources

import scala.collection.mutable
import scala.quoted.*

/** Global entry point for [[BeanMeta]] — compile-time dig, binary lookup, and runtime reflection.
  *
  * {{{
  * // Compile-time dig (macro, preserves generic precision)
  * val cm = MetaModels.of(classOf[User])
  *
  * // Lookup from beanmeta.idx (loaded at startup)
  * MetaModels.get(classOf[User])  // Option[BeanMeta]
  *
  * // Runtime reflection fallback
  * val cm = MetaModels.reflect(classOf[User])
  * }}}
  */
object MetaModels {

  /** Class name (JVM internal) -> BeanMeta, lazy loaded at first access. */
  private lazy val cache: Map[String, BeanMeta] = buildCache()

  /** Returns BeanMeta for the given class, or None if not found. */
  def get(clazz: Class[_]): Option[BeanMeta] = get(clazz.getName)

  /** Returns BeanMeta for the given class name (dot-separated or JVM internal), or None. */
  def get(className: String): Option[BeanMeta] = cache.get(normalize(className))

  /** Returns true if BeanMeta is available for the given class. */
  def contains(clazz: Class[_]): Boolean = contains(clazz.getName)

  /** Returns true if BeanMeta is available for the given class name. */
  def contains(className: String): Boolean = cache.contains(normalize(className))

  /** Returns all registered class names. */
  def classNames: Set[String] = cache.keySet

  /** Digs BeanMeta for classes at compile time (macro, preserves generic precision). */
  inline def of(inline clazzes: Class[_]*): List[BeanMeta] = ${ MetaDigger.digInto('clazzes) }

  /** Digs BeanMeta for a single class at compile time (macro). */
  inline def of[T](clazz: Class[T]): BeanMeta = ${ MetaDigger.digInto('clazz) }

  /** Reflects a class into BeanMeta via runtime reflection (fallback when no binary available). */
  def reflect(clazz: Class[_]): BeanMeta = MetaLoader.load(clazz)

  /** Loads all beanmeta.idx files from classpath into memory. */
  private def buildCache(): Map[String, BeanMeta] = {
    val map = mutable.HashMap.empty[String, BeanMeta]
    val stringPool = mutable.Set.empty[String] // shared dedup across all idx files
    val urls = Resources.load("classpath*:META-INF/beangle/beanmeta.idx")
    urls.foreach { url =>
      val in = new java.io.BufferedInputStream(url.openStream())
      try {
        MetaIndex.read(in, stringPool).foreach { cm =>
          map.put(normalize(cm.clazz.getName), cm)
        }
      } finally in.close()
    }
    map.toMap
  }

  /** Normalizes a class name to JVM internal format (dot-separated -> slash-separated). */
  private def normalize(className: String): String = className.replace('.', '/')
}
