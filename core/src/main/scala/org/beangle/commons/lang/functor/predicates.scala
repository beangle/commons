/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.commons.lang.functor

import org.beangle.commons.lang.Strings._
import java.util.Collection
/**
 * 有效整型判断谓词
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

object NotZero extends Predicate[Number] {

  def apply(value: Number): Boolean = {
    0 != value.asInstanceOf[Number].intValue
  }

}

object NotEmpty extends Predicate[String] {

  def apply(value: String): Boolean = (null != value) && (value.isInstanceOf[String]) && isNotEmpty(value.asInstanceOf[String])

}

object SingleWord extends Predicate[String] {

  def apply(str: String): Boolean = str.indexOf(DELIMITER) == -1
}

class InStr(val str: String) extends Predicate[String] {

  def apply(arg0: String): Boolean = -1 != str.indexOf(arg0.toString)
}

class Contains[T](val objs: Collection[_ <: T]) extends Predicate[T]() {

  def apply(arg0: T): Boolean = objs.contains(arg0)
}

class Max1Element extends Predicate[Collection[_]] {

  def apply(col: Collection[_]): Boolean = col.size < 2
}
