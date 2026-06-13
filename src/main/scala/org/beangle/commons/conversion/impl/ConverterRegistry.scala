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

package org.beangle.commons.conversion.impl

import org.beangle.commons.conversion.Converter

import java.lang.reflect.Modifier
import scala.collection.mutable

/** Mutable converter registry for the registration phase.
 *
 * Call `build()` to obtain an immutable snapshot for `GenericConversion`.
 * Not thread-safe; complete registration on one thread before building.
 *
 * @author chaostone
 * @since 3.2.0
 */
class ConverterRegistry {

  private val converters = new mutable.HashMap[Class[_], Map[Class[_], GenericConverter]]

  /** Registers a converter.
   *
   * @param converter the converter to add
   */
  def add(converter: Converter[_, _]): Unit = {
    var key: (Class[_], Class[_]) = null
    for (m <- converter.getClass.getMethods if m.getName == "apply" && Modifier.isPublic(m.getModifiers) && !m.isBridge)
      key = (m.getParameterTypes()(0), m.getReturnType)
    if (null == key) throw new IllegalArgumentException("Cannot find convert type pair " + converter.getClass)
    val sourceType = key._1
    val adapter = new ConverterAdapter(converter, key)
    converters.get(sourceType) match {
      case Some(existed) => converters += (sourceType -> (existed + (key._2 -> adapter)))
      case _ => converters += (sourceType -> Map((key._2 -> adapter)))
    }
  }

  /** Registers a `GenericConverter` (e.g. `ConverterFactory`). */
  def add(converter: GenericConverter): Unit = {
    val key = converter.getTypeinfo
    val sourceType = key._1
    converters.get(sourceType) match {
      case Some(existed) =>
        converters += (key._1 -> (existed + (key._2 -> converter)))
      case _ => converters += (key._1 -> Map((key._2 -> converter)))
    }
  }

  /** Freezes registered converters into an immutable map. */
  def build(): Map[Class[_], Map[Class[_], GenericConverter]] = converters.toMap
}
