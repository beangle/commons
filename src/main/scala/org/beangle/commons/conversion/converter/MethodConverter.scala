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

import org.beangle.commons.conversion.impl.GenericConverter

import java.lang.reflect.{Constructor, Method}

/** Converts source to target via single-arg constructor. */
class CtorConverter[T](st: Class[_], tt: Class[_], ctor: Constructor[_]) extends GenericConverter {
  override def getTypeinfo: (Class[_], Class[_]) = {
    (st, tt)
  }

  override def convert[T](input: Any, targetType: Class[T]): T = {
    ctor.newInstance(input).asInstanceOf[T]
  }
}

/** Converts source to target via companion apply method. */
class MethodConverter[T](st: Class[_], tt: Class[_], factory: Any, method: Method) extends GenericConverter {
  override def getTypeinfo: (Class[_], Class[_]) = {
    (st, tt)
  }

  override def convert[T](input: Any, targetType: Class[T]): T = {
    method.invoke(factory, input).asInstanceOf[T]
  }
}
