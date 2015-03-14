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
package org.beangle.commons.web.access

import java.text.DateFormat
import java.text.SimpleDateFormat
/**
 * Access Request
 *
 * @author chaostone
 * @since 3.0.1
 */
@SerialVersionUID(1L)
class AccessRequest extends Serializable {

  /**
   * 会话ID
   */
  var sessionid: String = _

  /**
   * 用户名
   */
  var username: String = _

  /**
   * Response status
   */
  var status: Int = 200

  /**
   * 资源
   */
  var uri: String = _

  /**
   * 查询字符串
   */
  var params: String = _

  /**
   * 开始时间
   */
  var beginAt: Long = _

  /**
   * 结束时间
   */
  var endAt: Long = 0

  def this(sessionid: String, username: String, resource: String) {
    this()
    this.sessionid = sessionid
    this.username = username
    this.uri = resource
    this.beginAt = System.currentTimeMillis()
  }

  def getDuration(): Long = {
    if (0 == endAt) System.currentTimeMillis() - beginAt else endAt - beginAt
  }

  override def toString(): String = {
    val sb = new StringBuilder(uri)
    sb.append('(')
    val f = new SimpleDateFormat("HH:mm:ss")
    sb.append(f.format(beginAt))
    sb.append('-')
    if (0 != endAt) {
      sb.append(f.format(endAt))
      sb.append(" duration ").append((endAt - beginAt) / 1000)
        .append(" s")
    }
    sb.append(')')
    sb.toString
  }
}
