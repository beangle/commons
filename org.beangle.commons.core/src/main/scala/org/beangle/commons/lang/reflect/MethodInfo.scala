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
package org.beangle.commons.lang.reflect

import java.lang.Character.isUpperCase
import org.beangle.commons.lang.Strings.substringAfter
import org.beangle.commons.lang.Strings.uncapitalize
import java.lang.reflect.Method
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.tuple.Pair

/**
 * Method name and return type and parameters type
 *
 * @author chaostone
 * @since 3.2.0
 */
class MethodInfo(val index: Int, val method: Method, val parameterTypes: Array[Class[_]])
    extends Comparable[MethodInfo]() {

  /**
   * Return thid method is property read method (0) or write method(1) or none(-1).
   */
  def property(): Option[Pair[Boolean, String]] = {
    val name = method.getName
    if (name.length > 3 && name.startsWith("get") && isUpperCase(name.charAt(3)) && 
      parameterTypes.length == 0) {
      return Some(Pair.of(true, uncapitalize(substringAfter(name, "get"))))
    } else if (name.length > 2 && name.startsWith("is") && isUpperCase(name.charAt(2)) && 
      parameterTypes.length == 0) {
      return Some(Pair.of(true, uncapitalize(substringAfter(name, "is"))))
    } else if (name.length > 3 && name.startsWith("set") && isUpperCase(name.charAt(3)) && 
      parameterTypes.length == 1) {
      return Some(Pair.of(false, uncapitalize(substringAfter(name, "set"))))
    }
    None
  }

  override def compareTo(o: MethodInfo): Int = this.index - o.index

  def matches(args:  Any*): Boolean = {
    if (parameterTypes.length != args.length) return false
    for (i <- 0 until args.length if null != args(i) && !parameterTypes(i).isInstance(args(i))) return false
    true
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
      for (`type` <- parameterTypes) {
        sb.append(`type`.getSimpleName).append(",")
      }
      sb.deleteCharAt(sb.length - 1)
      sb.append(')')
    }
    sb.toString
  }

  override def hashCode(): Int = {
    var hash = 0
    for (t <- parameterTypes) hash += t.hashCode
    method.getName.hashCode + hash
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
