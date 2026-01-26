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

package org.beangle.commons.net.ws

import org.beangle.commons.activation.{MediaType, MediaTypes}
import org.beangle.commons.codec.binary.Base64
import org.beangle.commons.lang.Strings.split
import org.beangle.commons.lang.{Charsets, Strings}
import org.beangle.commons.net.Networks
import org.beangle.commons.net.http.{HttpMethods, HttpUtils, Request, Response}

import java.net.{URL, URLEncoder}
import scala.collection.mutable

class RestRequest(val target: String, client: RestClient) {
  protected val headers = new mutable.HashMap[String, String]
  protected var authorization: Option[String] = None

  private var uri: String = ""
  private val queryParams = new mutable.ArrayBuffer[(String, String)]

  def header(name: String, value: String): RestRequest = {
    headers.put(name, value)
    this
  }

  def basic(username: String, password: String): RestRequest = {
    this.authorization = Some("Basic " + Base64.encode(s"$username:$password".getBytes))
    this
  }

  def bearer(token: String): RestRequest = {
    this.authorization = Some(s"Bearer $token")
    this
  }

  def request(acceptType: String): RestRequest = {
    headers.put("Accept", acceptType)
    this
  }

  def request(acceptType: MediaType): RestRequest = {
    headers.put("Accept", acceptType.toString)
    this
  }

  def path(uri: String, variables: Any*): RestRequest = {
    require(uri.startsWith("/"), "uri should starts with /")
    val parts = this.parse(uri)
    var resolved = uri
    require(parts.length == variables.size, "path variable number not matched.")
    parts.zip(variables) foreach { case (k, v) =>
      resolved = Strings.replace(resolved, s"{$k}", v.toString)
    }
    this.uri = resolved
    this
  }

  def param(name: String, value: Any): RestRequest = {
    this.queryParams.addOne(name, value.toString)
    this
  }

  def params(ps: (String, Any)*): RestRequest = {
    this.queryParams.addAll(ps.map(x => (x._1, x._2.toString)))
    this
  }

  def get(): Response = {
    HttpUtils.getData(buildURL(), Some({ c =>
      headers foreach { h => c.addRequestProperty(h._1, h._2) }
      authorization foreach { au => c.setRequestProperty("Authorization", au) }
      c.setConnectTimeout(client.connectTimeout)
      c.setReadTimeout(client.readTimeout)
    }))
  }

  def post(body: Any, contentType: String): Response = {
    val load = Request.build(body, contentType)
    load.headers(this.headers).auth(this.authorization)
    HttpUtils.invoke(buildURL(), HttpMethods.POST, load, Some({ c =>
      c.setConnectTimeout(client.connectTimeout)
      c.setReadTimeout(client.readTimeout)
    }))
  }

  def postJson(json: Any): Response = {
    val load = Request.asJson(json)
    this.post(load.body, MediaTypes.ApplicationJson.toString)
  }

  def postForm(params: (String, Any)*): Response = {
    val load = Request.asForm(params)
    this.post(load, load.contentType)
  }

  def delete(): Response = {
    val load = Request.build("", "application/json")
    load.headers(this.headers).auth(this.authorization)
    HttpUtils.delete(buildURL(), load)
  }

  def put(body: Any, contentType: String): Response = {
    val load = Request.build(body, contentType)
    load.headers(this.headers).auth(this.authorization)

    HttpUtils.invoke(buildURL(), HttpMethods.PUT, load, Some({ c =>
      c.setConnectTimeout(client.connectTimeout)
      c.setReadTimeout(client.readTimeout)
    }))
  }

  def putJson(json: Any): Response = {
    val load = Request.asJson(json)
    this.put(load.body, MediaTypes.ApplicationJson.toString)
  }

  def putForm(params: (String, Any)*): Response = {
    val load = Request.asForm(params)
    this.put(load.body, load.contentType)
  }

  protected[ws] def buildURL(): URL = {
    val hasParam = uri.indexOf("?") > 0
    val url = target + uri
    val sb = new StringBuilder(url)
    queryParams foreach { case (k, v) =>
      sb.append("&")
        .append(URLEncoder.encode(k, Charsets.UTF_8))
        .append('=')
        .append(URLEncoder.encode(v, Charsets.UTF_8))
    }
    if (!hasParam && queryParams.nonEmpty) {
      sb.setCharAt(url.length, '?')
    }
    Networks.url(sb.mkString)
  }

  /**
   * /a/b/c => ()
   * /{a}/&star/{c} => (a,c)
   * /a/b/{c}/{a*} => (c,a*)
   */
  private def parse(pattern: String): Seq[String] = {
    val parts = split(pattern, "/")
    val params = new collection.mutable.ArrayBuffer[String]
    var i = 0
    while (i < parts.length) {
      val p = parts(i)
      if (p.charAt(0) == '{' && p.charAt(p.length - 1) == '}') {
        params.addOne(p.substring(1, p.length - 1))
      }
      i += 1
    }
    params.toSeq
  }

}
