/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang.conversion.impl

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.conversion.Converter
import org.beangle.commons.lang.tuple.Pair
import scala.collection.JavaConversions._
import org.beangle.commons.lang.Objects

/**
 * A converter factory that can convert objects from S to subtypes of R.
 *
 * @author chaostone
 * @since 3.2.0
 * @param <S>
 * @param <R> The target base
 */
abstract class ConverterFactory[S, R] extends GenericConverter {

  protected var converters: Map[Class[_], Converter[S, _ <: R]] = CollectUtils.newHashMap()

  /**
   * Return convert from S to T
   */
  def getConverter[T <: R](targetType: Class[T]): Converter[S, T] = {
    converters.get(targetType).asInstanceOf[Converter[S, T]]
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
      Pair.of[Class[_], Class[_]](classof(ptype.getActualTypeArguments()(0)), classof(ptype.getActualTypeArguments()(1)))
    } else {
      throw new RuntimeException("Cannot identify type of " + getClass)
    }
  }

  override def convert(input: Any, sourceType: Class[_], targetType: Class[_]): Any = {
    val converter = getConverter(targetType.asInstanceOf[Class[R]])
    if ((null == converter)) Objects.default(targetType) else converter.apply(input.asInstanceOf[S])
  }

  protected def register(targetType: Class[_], converter: Converter[S, _ <: R]) {
    converters.put(targetType, converter)
  }
}
