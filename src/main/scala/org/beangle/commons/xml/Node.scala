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

package org.beangle.commons.xml

import org.beangle.commons.collection.Collections

/** XML node interface (label, attributes, children). */
trait Node {

  def label: String

  def attrs: collection.Map[String, String]

  def children: collection.Seq[Node]

  /** Gets attribute value by name. */
  def get(name: String): Option[String]

  /** Concatenated text content. */
  def text: String

  /** Returns direct children with the given label. */
  final def children(name: String): collection.Seq[Node] = {
    children.filter(_.label == name)
  }

  /** Returns all descendants with the given label (recursive). */
  final def descendants(name: String): collection.Seq[Node] = {
    val newNodes = Collections.newBuffer[Node]
    children.map { cn =>
      if (cn.label == name) {
        newNodes.addOne(cn)
      } else {
        newNodes.addAll(cn.descendants(name))
      }
    }
    newNodes
  }

  /** Gets attribute value or default. */
  final def get(name: String, defaultValue: String): String = {
    get(name).getOrElse(defaultValue)
  }

  /** Returns true if the attribute exists. */
  final def has(name: String): Boolean = {
    get(name).nonEmpty
  }

  /** Gets attribute value; throws if missing. */
  final def apply(name: String): String = {
    get(name) match {
      case Some(v) => v
      case None => throw IllegalArgumentException(name)
    }
  }

  /** Queries direct children. */
  def \(name: String): NodeSeq = {
    NodeSeq.of(this) \ name
  }

  /** Queries descendants. */
  def \\(name: String): NodeSeq = {
    NodeSeq.of(this) \\ name
  }
}
