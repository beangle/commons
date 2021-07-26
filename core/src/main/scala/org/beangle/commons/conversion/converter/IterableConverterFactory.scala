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

import java.lang.{ Iterable => JIterable }
import java.{ util => ju }
import scala.collection.{ immutable, mutable }

object IterableConverterFactory extends ConverterFactory[JIterable[_], scala.collection.Iterable[_]] {

  //seq
  register(classOf[mutable.Seq[_]], new SeqConverter(false))
  register(classOf[collection.Seq[_]], new SeqConverter(false))
  register(classOf[immutable.Seq[_]], new SeqConverter(true))

  //buffer
  register(classOf[mutable.Seq[_]], BufferConverter)

  //set
  register(classOf[mutable.Set[_]], new SetConverter(false))
  register(classOf[collection.Set[_]], new SetConverter(false))
  register(classOf[immutable.Set[_]], new SetConverter(true))

  //iterable
  register(classOf[collection.Iterable[_]], CollectionConverter)

  import scala.jdk.javaapi.CollectionConverters.asScala

  class SeqConverter(immutable: Boolean) extends Converter[JIterable[_], collection.Seq[_]] {
    override def apply(it: JIterable[_]): collection.Seq[_] =
      it match {
        case l: ju.List[_] => if (immutable) asScala(l).toList else asScala(l)
        case _ => null
      }
  }

  object BufferConverter extends Converter[JIterable[_], mutable.Buffer[_]] {
    override def apply(it: JIterable[_]): mutable.Buffer[_] =
      it match {
        case l: ju.List[_] => asScala(l)
        case _ => null
      }
  }

  class SetConverter(immutable: Boolean) extends Converter[JIterable[_], collection.Set[_]] {
    override def apply(it: JIterable[_]): collection.Set[_] =
      it match {
        case l: ju.Set[_] => if (immutable) asScala(l).toSet else asScala(l)
        case _ => null
      }
  }

  object CollectionConverter extends Converter[JIterable[_], collection.Iterable[_]] {
    override def apply(it: JIterable[_]): collection.Iterable[_] =
      it match {
        case c: ju.Collection[_] => asScala(c)
        case i: java.lang.Iterable[_] => asScala(i)
        case null => null
      }
  }
}
