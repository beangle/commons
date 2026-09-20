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

package org.beangle.commons.conversion.converter

import org.beangle.commons.conversion.Converter
import org.beangle.commons.conversion.impl.ConverterFactory

import java.lang.Iterable as JIterable
import java.util as ju
import scala.collection.{immutable, mutable}

/** Converts java.util.Collection/List/Set to Scala Seq/Buffer/Set/Iterable. */
object IterableConverterFactory extends ConverterFactory[JIterable[?], scala.collection.Iterable[?]] {

  //seq
  register(classOf[mutable.Seq[?]], new SeqConverter(false))
  register(classOf[collection.Seq[?]], new SeqConverter(false))
  register(classOf[immutable.Seq[?]], new SeqConverter(true))

  //buffer
  register(classOf[mutable.Seq[?]], BufferConverter)

  //set
  register(classOf[mutable.Set[?]], new SetConverter(false))
  register(classOf[collection.Set[?]], new SetConverter(false))
  register(classOf[immutable.Set[?]], new SetConverter(true))

  //iterable
  register(classOf[collection.Iterable[?]], CollectionConverter)

  import scala.jdk.javaapi.CollectionConverters.asScala

  class SeqConverter(immutable: Boolean) extends Converter[JIterable[?], collection.Seq[?]] {
    override def apply(it: JIterable[?]): collection.Seq[?] =
      it match {
        case l: ju.List[_] => if (immutable) asScala(l).toList else asScala(l)
        case _ => null
      }
  }

  object BufferConverter extends Converter[JIterable[?], mutable.Buffer[?]] {
    override def apply(it: JIterable[?]): mutable.Buffer[?] =
      it match {
        case l: ju.List[_] => asScala(l)
        case _ => null
      }
  }

  class SetConverter(immutable: Boolean) extends Converter[JIterable[?], collection.Set[?]] {
    override def apply(it: JIterable[?]): collection.Set[?] =
      it match {
        case l: ju.Set[_] => if (immutable) asScala(l).toSet else asScala(l)
        case _ => null
      }
  }

  object CollectionConverter extends Converter[JIterable[?], collection.Iterable[?]] {
    override def apply(it: JIterable[?]): collection.Iterable[?] =
      it match {
        case c: ju.Collection[_] => asScala(c)
        case i: java.lang.Iterable[_] => asScala(i)
        case null => null
      }
  }
}
