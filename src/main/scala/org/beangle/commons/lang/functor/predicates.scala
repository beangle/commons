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

package org.beangle.commons.lang.functor

import org.beangle.commons.lang.Strings.*

import java.util as ju

/** Predicate: value is a valid integer within the given range.
 *
 * @author chaostone
 */
class InRange(val floor: Int, val upper: Int) extends Predicate[Number] {

  def apply(value: Number): Boolean = {
    if (null == value) {
      false
    } else {
      val valueInt = value.intValue()
      valueInt <= upper && valueInt >= floor
    }
  }
}

/** Predicate: value is non-null and non-zero. */
object NotZero extends Predicate[Number] {

  def apply(value: Number): Boolean =
    null != value && 0 != value.asInstanceOf[Number].intValue
}

/** Predicate: value is non-null and non-blank. */
object NotEmpty extends Predicate[String] {

  def apply(value: String): Boolean = {
    (null != value) && isNotEmpty(value)
  }
}

/** Predicate: string contains no delimiter. */
object SingleWord extends Predicate[String] {

  def apply(str: String): Boolean = {
    str.indexOf(DELIMITER) == -1
  }
}

/** Predicate: str contains the argument. */
class InStr(val str: String) extends Predicate[String] {

  def apply(arg0: String): Boolean = {
    -1 != str.indexOf(arg0)
  }
}

/** Predicate: value is in the given collection. */
class Contains[T](val objs: ju.Collection[_ <: T]) extends Predicate[T]() {

  def apply(arg0: T): Boolean = objs.contains(arg0)
}

/** Predicate: collection has at most one element. */
class Max1Element extends Predicate[ju.Collection[_]] {

  def apply(col: ju.Collection[_]): Boolean = {
    col.size < 2
  }
}
