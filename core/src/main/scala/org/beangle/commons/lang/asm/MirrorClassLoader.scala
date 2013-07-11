/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang.asm

import java.lang.reflect.Method
import scala.collection.mutable

object MirrorClassLoader {

  private val proxyClassLoaders = new mutable.HashMap[ClassLoader, MirrorClassLoader]

  def get(clazz: Class[_]): MirrorClassLoader = {
    val parent = clazz.getClassLoader
    if (null == parent) return null
    var loader = proxyClassLoaders.get(parent).orNull
    if (null == loader) {
      proxyClassLoaders.synchronized {
        loader = proxyClassLoaders.get(parent).orNull
        if (null == loader) {
          loader = new MirrorClassLoader(parent)
          proxyClassLoaders.put(parent, loader)
        }
      }
    }
    loader
  }
}

/**
 * ProxyClassLoader using bean's original classLoader define class
 *
 * @author chaostone
 * @since 3.2.0
 */
class MirrorClassLoader private (parent: ClassLoader) extends ClassLoader(parent) {

  def defineClass(name: String, bytes: Array[Byte]): Class[_] = {
    try {
      val method = classOf[ClassLoader].getDeclaredMethod("defineClass", classOf[String], classOf[Array[Byte]], classOf[Int], classOf[Int])
      method.setAccessible(true)
      return method.invoke(getParent, Array(name, bytes, 0, bytes.length)).asInstanceOf[Class[_]]
    } catch {
      case ignored: Exception =>
    }
    defineClass(name, bytes, 0, bytes.length)
  }
}
