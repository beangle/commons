/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.lang

import java.{ lang => jl }
/**
 * Wrap or Unwrap primitive
 *
 * @author chaostone
 * @since 3.2.0
 */
object Primitives {

  /**
   * Primitive types to their corresponding wrapper types.
   */
  private val primitiveToWrappers: Map[Class[_], Class[_]] = Map((classOf[Boolean], classOf[jl.Boolean]),
    (classOf[Byte], classOf[jl.Byte]), (classOf[Char], classOf[jl.Character]),
    (classOf[Int], classOf[jl.Integer]), (classOf[Short], classOf[jl.Short]),
    (classOf[Long], classOf[jl.Long]), (classOf[Float], classOf[jl.Float]),
    (classOf[Double], classOf[jl.Double]))

  /**
   * Wrapper types to their corresponding primitive types.
   */
  private val wrapperToPrimitives = primitiveToWrappers.map(_.swap)

  def default[T](clazz: Class[T]): T = {
    if (clazz.isPrimitive) {
      if (clazz.isAssignableFrom(classOf[Boolean])) false.asInstanceOf[T]
      else 0.asInstanceOf[T]
    } else null.asInstanceOf[T]
  }
  /**
   * Returns {@code true} if {@code type} is one of the nine
   * primitive-wrapper types, such as {@link Integer}.
   *
   * @see Class#isPrimitive
   */
  def isWrapperType(clazz: Class[_]): Boolean = wrapperToPrimitives.contains(clazz)

  /**
   * Returns the corresponding wrapper type of {@code type} if it is a primitive
   * type; otherwise returns {@code type} itself. Idempotent.
   *
   * <pre>
   *     wrap(int.class) == Integer.class
   *     wrap(Integer.class) == Integer.class
   *     wrap(String.class) == String.class
   * </pre>
   */
  def wrap[T](clazz: Class[T]): Class[T] = {
    if ((clazz.isPrimitive || (clazz eq classOf[Unit]))) primitiveToWrappers.get(clazz).get.asInstanceOf[Class[T]] else clazz
  }

  /**
   * Returns the corresponding primitive type of {@code type} if it is a
   * wrapper type; otherwise returns {@code type} itself. Idempotent.
   *
   * <pre>
   *     unwrap(Integer.class) == int.class
   *     unwrap(int.class) == int.class
   *     unwrap(String.class) == String.class
   * </pre>
   */
  def unwrap[T](clazz: Class[T]): Class[T] = wrapperToPrimitives.get(clazz).getOrElse(clazz).asInstanceOf[Class[T]]
}
