/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.conversion.impl

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import org.beangle.commons.conversion.Converter
import org.beangle.commons.lang.Objects
import scala.collection.mutable
/**
 * A converter factory that can convert objects from S to subtypes of R.
 *
 * @author chaostone
 * @since 3.2.0
 * @param <S>
 * @param <R> The target base
 */
abstract class ConverterFactory[S, R] extends GenericConverter {

  protected val converters = new mutable.HashMap[Class[_], Converter[S, _ <: R]]

  /**
   * Return convert from S to T
   */
  def getConverter[T <: R](targetType: Class[T]): Option[Converter[S, T]] = {
    converters.get(targetType).asInstanceOf[Option[Converter[S, T]]]
  }

  private def classof(clazz: Type): Class[_] = {
    if (clazz.isInstanceOf[Class[_]]) {
      return clazz.asInstanceOf[Class[_]]
    } else if (clazz.isInstanceOf[ParameterizedType]) {
      return clazz.asInstanceOf[ParameterizedType].getRawType.asInstanceOf[Class[_]]
    }
    null
  }

  override def getTypeinfo(): Pair[Class[_], Class[_]] = {
    val superType = getClass.getGenericSuperclass
    if ((superType.isInstanceOf[ParameterizedType])) {
      val ptype = superType.asInstanceOf[ParameterizedType]
      //Pair.of[Class[_], Class[_]]
      (classof(ptype.getActualTypeArguments()(0)), classof(ptype.getActualTypeArguments()(1)))
    } else {
      throw new RuntimeException("Cannot identify type of " + getClass)
    }
  }

  override def convert(input: Any, sourceType: Class[_], targetType: Class[_]): Any = {
    getConverter(targetType.asInstanceOf[Class[R]]) match {
      case Some(converter) => converter.apply(input.asInstanceOf[S])
      case _ => Objects.default(targetType)
    }
  }

  protected def register(targetType: Class[_], converter: Converter[S, _ <: R]) {
    converters.put(targetType, converter)
  }
}