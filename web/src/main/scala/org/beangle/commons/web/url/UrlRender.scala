/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.web.url

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings

class UrlRender {

  var escapeAmp: Boolean = _

  def render(context: String, referer: String, uri: String, params: Map[String, String]): String = {
    var separator = "&"
    if (escapeAmp) separator = "&amp;"
    val sb = renderUri(context, referer, uri)
    sb.append(separator)
    for ((key, value) <- params) {
      try {
        sb.append(key).append('=').append(URLEncoder.encode(value, "UTF-8")).append(separator)
      } catch {
        case e: UnsupportedEncodingException => e.printStackTrace()
      }
    }
    sb.delete(sb.length - separator.length, sb.length)
    sb.toString
  }

  def render(context: String, referer: String, uri: String, params: String*): String = {
    var separator = "&"
    if (escapeAmp) separator = "&amp;"

    val sb = renderUri(context, referer, uri)
    sb.append(separator)
    for (param <- params) {
      try {
        sb.append(URLEncoder.encode(param, "UTF-8"))
        sb.append(separator)
      } catch {
        case e: UnsupportedEncodingException => e.printStackTrace()
      }
    }
    sb.delete(sb.length - separator.length, sb.length)
    sb.toString
  }

  def render(context: String, referer: String, uri: String): String = {
    renderUri(context, referer, uri).toString
  }

  private def renderUri(context: String, referer: String, uriStr: String): StringBuilder = {
    val sb = new StringBuilder()
    if (Strings.isEmpty(uriStr)) {
      sb ++= referer
      return sb
    }
    var questIndex = uriStr.indexOf('?')
    val queryStr = if (-1 != questIndex) uriStr.substring(questIndex + 1) else null
    val uri = if (-1 == questIndex) uriStr else uriStr.substring(0, questIndex)
    if (-1 == questIndex) questIndex = uriStr.length

    sb ++= context
    if (uri.startsWith("/")) {
      sb ++= uri.substring(0, questIndex)
    } else {
      val lastslash = referer.lastIndexOf("/") + 1
      val namespace = referer.substring(0, lastslash)
      sb.append(namespace)
      if (uri.startsWith("!")) {
        var dot = referer.indexOf("!", lastslash)
        if (-1 == dot) dot = referer.indexOf(".", lastslash)
        if (-1 == dot) dot = referer.length
        val action = referer.substring(lastslash, dot)
        sb ++= action
        sb ++= uri
      } else {
        sb.append(uri)
      }
    }
    if (null != queryStr) sb.append('?').append(queryStr)
    sb
  }
}
