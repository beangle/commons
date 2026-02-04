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

object NodeSeq {
  val Empty = new NodeSeq(List.empty)

  def of(n: Node): NodeSeq = {
    NodeSeq(List(n))
  }
}

class NodeSeq(elems: Iterable[Node]) extends Iterable[Node] {
  override def iterator: Iterator[Node] = elems.iterator

  def \\(name: String): NodeSeq = {
    if elems.isEmpty then this
    else new NodeSeq(elems.flatMap(c => c.descendants(name)))
  }

  def \(name: String): NodeSeq = {
    if elems.isEmpty then this
    else {
      name match {
        case "_" => new NodeSeq(elems.flatMap(c => c.children))
        case _ if name(0) == '@' && this.elems.size == 1 =>
          val head = this.elems.head.get(name.substring(1))
          if (head.isEmpty) NodeSeq.Empty else NodeSeq.of(new Text(head.get))
        case _ => new NodeSeq(elems.flatMap(c => c.children(name)))
      }
    }
  }

  def text: String = {
    if elems.isEmpty then ""
    else {
      assert(elems.size == 1)
      elems.head.text
    }
  }
}
