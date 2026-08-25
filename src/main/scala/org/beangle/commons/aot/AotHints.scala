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

package org.beangle.commons.aot

import org.beangle.commons.collection.Collections

/** Mutable container for ahead-of-time hints used by GraalVM native-image.
  *
  * Subclasses or callers register types, resource patterns, proxy interfaces,
  * and serializable classes via the register methods, then pass this container
  * to [[AotHintGenerator]] for JSON config file generation.
  *
  * {{{
  * val hints = new AotHints
  * hints.registerType(classOf[User], classOf[Role])
  * hints.registerPattern("META-INF/custom.idx")
  * hints.registerProxy(classOf[UserService])
  * hints.registerSerializable(classOf[UserDto])
  * AotHintGenerator.write(outDir, hints)
  * }}}
  */
class AotHints {

  private val types = Collections.newSet[Class[_]]
  private val patterns = Collections.newSet[String]
  private val proxies = Collections.newSet[List[Class[_]]]
  private val serializables = Collections.newSet[Class[_]]

  /** Registers classes for reflection access in native-image. */
  def registerType(classes: Class[_]*): Unit = {
    val it = classes.iterator
    while it.hasNext do types.add(it.next())
  }

  /** Registers resource inclusion patterns (ant-style globs). */
  def registerPattern(patterns: String*): Unit = {
    val it = patterns.iterator
    while it.hasNext do this.patterns.add(it.next())
  }

  /** Registers a set of interfaces for JDK dynamic proxy. */
  def registerProxy(interfaces: Class[_]*): Unit = {
    proxies.add(interfaces.toList)
  }

  /** Registers classes supporting Java serialization. */
  def registerSerializable(classes: Class[_]*): Unit = {
    val it = classes.iterator
    while it.hasNext do serializables.add(it.next())
  }

  /** Returns all registered reflection types. */
  def getTypes: collection.Set[Class[_]] = types

  /** Returns all registered resource patterns. */
  def getPatterns: collection.Set[String] = patterns

  /** Returns all registered proxy interface sets. */
  def getProxies: collection.Set[List[Class[_]]] = proxies

  /** Returns all registered serializable classes. */
  def getSerializables: collection.Set[Class[_]] = serializables

  /** Returns true if no hints have been registered. */
  def isEmpty: Boolean =
    types.isEmpty && patterns.isEmpty && proxies.isEmpty && serializables.isEmpty

  /** Merges all hints from another [[AotHints]] into this one. */
  def addAll(other: AotHints): Unit = {
    types.addAll(other.types)
    patterns.addAll(other.patterns)
    proxies.addAll(other.proxies)
    serializables.addAll(other.serializables)
  }
}
