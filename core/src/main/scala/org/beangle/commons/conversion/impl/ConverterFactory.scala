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
import org.beangle.commons.lang.Objects

import java.lang.reflect.{ParameterizedType, Type}
import scala.collection.mutable

/**
  * A converter factory that can convert objects from S to subtypes of R.
  *
  * @author chaostone
  * @since 3.2.0
  * @param < S>
  * @param < R> The target base
  */
abstract class ConverterFactory[S, R] extends GenericConverter {

  protected val converters = new mutable.HashMap[Class[_], Converter[S, _]]

  /**
    * Return convert from S to T
    */
  def getConverter[T](targetType: Class[T]): Option[Converter[S, T]] =
    converters.get(targetType).asInstanceOf[Option[Converter[S, T]]]

  private def classof(clazz: Type): Class[_] = {
    clazz match
      case value: Class[_] => value
      case parameterizedType: ParameterizedType => parameterizedType.getRawType.asInstanceOf[Class[_]]
      case _ => null
  }

  override def getTypeinfo: (Class[_], Class[_]) = {
    val superType = getClass.getGenericSuperclass
    superType match
      case ptype: ParameterizedType =>
        (classof(ptype.getActualTypeArguments()(0)), classof(ptype.getActualTypeArguments()(1)))
      case _ => throw new RuntimeException("Cannot identify type of " + getClass)
  }

  override def convert[T](input: Any, targetType: Class[T]): T = {
    getConverter(targetType) match
      case Some(converter) => converter.apply(input.asInstanceOf[S])
      case _ => Objects.default(targetType)
  }

  protected def register(targetType: Class[_], converter: Converter[S, _]): Unit = converters.put(targetType, converter)
}
