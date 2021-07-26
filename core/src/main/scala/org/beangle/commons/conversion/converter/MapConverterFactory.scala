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

import java.{ util => ju }
import scala.collection.{ immutable, mutable }

object MapConverterFactory extends ConverterFactory[ju.Map[_, _], scala.collection.Map[_, _]] {

  register(classOf[mutable.Map[_, _]], new MapConverter(false))
  register(classOf[collection.Map[_, _]], new MapConverter(false))
  register(classOf[immutable.Map[_, _]], new MapConverter(true))

  import scala.jdk.javaapi.CollectionConverters.asScala

  class MapConverter(immutable: Boolean) extends Converter[ju.Map[_, _], collection.Map[_, _]] {
    override def apply(it: ju.Map[_, _]): collection.Map[_, _] = {
      val result: collection.Map[_, _] =
        it match {
          case cm: ju.concurrent.ConcurrentMap[_, _] => asScala(cm)
          case p: ju.Properties => asScala(p)
          case m: ju.Map[_, _] => asScala(m)
          case null => null.asInstanceOf[collection.Map[_, _]]
        }
      if (immutable)
        if (null == result) null else result.toMap
      else
        result
    }
  }
}
