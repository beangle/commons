/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.data.dao

import org.beangle.commons.lang.Strings
object Condition {
  def apply(content: String, initParams: Any*) = new Condition(content, initParams: _*)
}
/**
 * 查询条件 使用例子如下
 * <p>
 * <blockquote>
 *
 * <pre>
 *      new Condition(&quot;std.id=?&quot;,new Long(2));
 *      或者 Condition(&quot;std.id=:std_id&quot;,new Long(2));
 *      ?绑定单值.命名参数允许绑定多值.但是只能由字母,数组和下划线组成
 *      一组condition只能采取上面一种形式
 * </pre>
 *
 * </blockquote>
 * <p>
 *
 * @author chaostone
 */
class Condition(val content: String, initParams: Any*) {

  val params = new collection.mutable.ListBuffer[Any]
  params ++= initParams

  /**
   * <p>
   * isNamed.
   * </p>
   *
   * @return a boolean.
   */
  def named: Boolean = !Strings.contains(content, "?")

  /**
   * 得到查询条件中所有的命名参数.
   */
  def paramNames: List[String] = {
    if (!Strings.contains(content, ":")) return Nil
    val names = new collection.mutable.ListBuffer[String]
    var index = 0;
    var colonIndex = content.indexOf(':', index)
    while (index < content.length && colonIndex > -1) {
      index = colonIndex + 1;
      while (index < content.length && isValidIdentifierStarter(content.charAt(index))) {
        index += 1
      }
      val paramName = content.substring(colonIndex + 1, index);
      if (!names.contains(paramName)) names += paramName
      colonIndex = content.indexOf(':', index)
    }
    names.toList
  }

  def isValidIdentifierStarter(ch: Char): Boolean = {
    (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || (ch == '_') || ('0' <= ch && ch <= '9'));
  }

  /**
   * <p>
   * param.
   * </p>
   *
   * @param value a {@link java.lang.Object} object.
   * @return a {@link org.beangle.commons.dao.query.builder.Condition} object.
   */
  def param(value: Any): this.type = {
    params += value
    this
  }

  /**
   * params.
   */
  def params(values: Seq[Any]): this.type = {
    params.clear()
    params ++= values
    this
  }

  /**
   * <p>
   * toString.
   * </p>
   *
   * @see java.lang.Object#toString()
   * @return a String object.
   */
  override def toString: String = {
    val str = new StringBuilder(content).append(" ");
    for (value <- params) {
      str.append(value)
    }
    str.mkString
  }

  override def equals(obj: Any): Boolean = obj match {
    case other: Condition => content.equals(other.content)
    case _ => false
  }

  /**
   * <p>
   * hashCode.
   * </p>
   *
   * @return a int.
   */
  override def hashCode(): Int = if (null == content) 0 else content.hashCode
}
