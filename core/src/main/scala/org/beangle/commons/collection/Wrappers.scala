/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.collection

import java.{util => ju}

object Wrappers {

  case class ImmutableJList[A](underlying: ju.List[A]) extends collection.immutable.Seq[A] {
    def length: Int = underlying.size

    override def isEmpty: Boolean = underlying.isEmpty

    import scala.jdk.CollectionConverters._
    override def iterator: Iterator[A] = underlying.iterator.asScala

    def apply(i: Int): A = underlying.get(i)
  }

}
