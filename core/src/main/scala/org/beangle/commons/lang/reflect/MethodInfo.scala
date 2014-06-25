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
package org.beangle.commons.lang.reflect

import java.lang.Character.isUpperCase
import org.beangle.commons.lang.Strings._
import java.lang.reflect.Method
import org.beangle.commons.lang.Objects

/**
 * Method name and return type and parameters type
 *
 * @author chaostone
 * @since 3.2.0
 */
class MethodInfo(val index: Int, val method: Method, val parameterTypes: Array[Class[_]]) extends Ordered[MethodInfo] {

  /**
   * Return this method is property read method (0) or write method(1) or none(-1).
   */
  def property(): Option[Tuple2[Boolean, String]] = {
    val name = method.getName
    if (0 == parameterTypes.length && method.getReturnType != classOf[Unit]) {
      val propertyName = if (name.startsWith("get") && name.length > 3 && isUpperCase(name.charAt(3)))
        uncapitalize(substringAfter(name, "get"))
      else if (name.startsWith("is") && name.length > 2 && isUpperCase(name.charAt(2)))
        uncapitalize(substringAfter(name, "is"))
      else name
      Some((true, propertyName))
    } else if (1 == parameterTypes.length) {
      val propertyName = if (name.startsWith("set") && name.length > 3 && isUpperCase(name.charAt(3)))
        uncapitalize(substringAfter(name, "set"))
      else if (name.endsWith("_$eq")) substringBefore(name, "_$eq")
      else null
      if (null == propertyName) None else Some((false, propertyName))

    } else None
  }

  override def compare(o: MethodInfo): Int = this.index - o.index

  def matches(args: Any*): Boolean = {
    if (parameterTypes.length != args.length) return false
    (0 until args.length).find { i =>
      null != args(i) && !parameterTypes(i).isInstance(args(i))
    }.isEmpty
  }

  override def toString(): String = {
    val returnType = method.getReturnType
    val sb = new StringBuilder()
    sb.append(if ((null == returnType)) "void" else returnType.getSimpleName)
    sb.append(' ').append(method.getName)
    if (parameterTypes.length == 0) {
      sb.append("()")
    } else {
      sb.append('(')
      for (t <- parameterTypes) sb.append(t.getSimpleName).append(",")
      sb.deleteCharAt(sb.length - 1).append(')')
    }
    sb.toString
  }

  override def hashCode(): Int = {
    var hash = 0
    for (t <- parameterTypes) hash += t.hashCode
    hash + method.getName.hashCode
  }

  override def equals(obj: Any): Boolean = obj match {
    case obj: MethodInfo => {
      val other = obj
      Objects.equalsBuilder().add(method.getName, other.method.getName)
        .add(parameterTypes, other.parameterTypes)
        .isEquals
    }
    case _ => false
  }
}
