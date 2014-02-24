/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.web.url

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings

class UrlRender(var initSuffix: String = null) {

  val suffix = if (null != initSuffix && initSuffix.charAt(0) != '.') "." + initSuffix else initSuffix

  var escapeAmp: Boolean = _

  def render(referer: String, uri: String, params: Map[String, String]): String = {
    var separator = "&"
    if (escapeAmp) separator = "&amp;"
    val sb = renderUri(referer, uri)
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

  def render(referer: String, uri: String, params: String*): String = {
    var separator = "&"
    if (escapeAmp) {
      separator = "&amp;"
    }
    val sb = renderUri(referer, uri)
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

  def render(referer: String, uri: String): String = renderUri(referer, uri).toString

  private def renderUri(referer: String, uriStr: String): StringBuilder = {
    val sb = new StringBuilder()
    if (Strings.isEmpty(uriStr)) {
      sb ++= referer
      return sb
    }
    var questIndex = uriStr.indexOf('?')
    val queryStr = if (-1 != questIndex) uriStr.substring(questIndex + 1) else null
    val uri = if (-1 == questIndex) uriStr else uriStr.substring(0, questIndex)
    if (-1 == questIndex) questIndex = uriStr.length

    if (uri.startsWith("/")) {
      val rirstslash = referer.indexOf("/", 1)
      val context = if ((-1 == rirstslash)) "" else referer.substring(0, rirstslash)
      sb ++= context
      sb ++= uri.substring(0, questIndex)
    } else {
      val lastslash = referer.lastIndexOf("/")
      val namespace = referer.substring(0, lastslash)
      sb.append(namespace)
      if (uri.startsWith("!")) {
        var dot = referer.indexOf("!", lastslash)
        if (-1 == dot) {
          dot = referer.indexOf(".", lastslash)
        }
        dot = if ((-1 == dot)) referer.length else dot
        val action = referer.substring(lastslash, dot)
        sb ++= action
        sb ++= uri
      } else {
        sb.append('/').append(uri)
      }
    }
    if (null != suffix) sb.append(suffix)
    if (null != queryStr) {
      sb.append('?').append(queryStr)
    }
    sb
  }
}