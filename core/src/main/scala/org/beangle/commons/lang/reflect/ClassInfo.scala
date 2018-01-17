/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
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

import java.lang.reflect.Method

import scala.collection.mutable
import scala.language.existentials

import org.beangle.commons.lang.Objects

object ClassInfo {
  def apply(methodInfos: Seq[MethodInfo]): ClassInfo = {
    new ClassInfo(methodInfos.groupBy(info => info.method.getName))
  }
}

/**
 * Class meta information.It contains method signature,property names
 */
class ClassInfo(val methods: Map[String, Seq[MethodInfo]]) {
  /**
   * Return public metheds according to given name
   */
  def getMethods(name: String): Seq[MethodInfo] = {
    methods.get(name).getOrElse(Seq.empty)
  }

  /**
   * Return all public methods.
   */
  def methodList: List[MethodInfo] = {
    val rs = new mutable.ListBuffer[MethodInfo]
    for ((key, value) <- methods; info <- value) rs += info
    rs.sorted.toList
  }

}

/**
 * Method name and return type and parameters type
 */
final class MethodInfo(val method: Method, val parameterTypes: Array[Class[_]], val returnType: Class[_])
    extends Ordered[MethodInfo] {

  override def compare(o: MethodInfo): Int = {
    this.method.getName.compareTo(o.method.getName)
  }

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
      Objects.equalsBuilder.add(method.getName, obj.method.getName)
        .add(parameterTypes, obj.parameterTypes)
        .isEquals
    }
    case _ => false
  }
}
