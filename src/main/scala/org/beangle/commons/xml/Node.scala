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

trait Node {

  def label: String

  def children: Iterable[Node]

  def get(name: String): Option[String]

  def text: String

  final def children(name: String): Iterable[Node] = {
    children.filter(_.label == name)
  }

  final def descendants(name: String): Iterable[Node] = {
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

  final def get(name: String, defaultValue: String): String = {
    get(name).getOrElse(defaultValue)
  }

  final def has(name: String): Boolean = {
    get(name).nonEmpty
  }

  final def apply(name: String): String = {
    get(name) match {
      case Some(v) => v
      case None => throw IllegalArgumentException(name)
    }
  }

  def \(name: String): NodeSeq = {
    NodeSeq.of(this) \ name
  }

  def \\(name: String): NodeSeq = {
    NodeSeq.of(this) \\ name
  }
}
