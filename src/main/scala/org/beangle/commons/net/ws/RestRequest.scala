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
import org.beangle.commons.net.http.{HttpMethods, HttpUtils, Request, Response}

import java.net.URLEncoder
import java.net.http.HttpClient
import scala.collection.mutable

/** RestRequest factory. */
object RestRequest {

  /** Creates RestRequest with default HttpClient. */
  def target(t: String): RestRequest = {
    new RestRequest(t, HttpUtils.Default)
  }

  /** Creates RestRequest with custom HttpClient. */
  def target(t: String, client: HttpClient): RestRequest = {
    new RestRequest(t, HttpUtils.withClient(client))
  }
}

/** Fluent REST API client builder. */
class RestRequest private(val target: String, private val httpUtils: HttpUtils) {
  assert(!target.endsWith("/"), "Endpoint should not endwith /")
  protected val headers = new mutable.HashMap[String, String]
  protected var authorization: Option[String] = None

  private var path: String = ""
  private val queryParams = new mutable.ArrayBuffer[(String, String)]

  /** Adds a request header. */
  def header(name: String, value: String): RestRequest = {
    headers.put(name, value)
    this
  }

  /** Sets Basic auth. */
  def basic(username: String, password: String): RestRequest = {
    this.authorization = Some("Basic " + Base64.encode(s"$username:$password".getBytes))
    this
  }

  /** Sets Bearer token auth. */
  def bearer(token: String): RestRequest = {
    this.authorization = Some(s"Bearer $token")
    this
  }

  /** Sets Accept header. */
  def request(acceptType: String): RestRequest = {
    headers.put("Accept", acceptType)
    this
  }

  /** Sets Accept header from MediaType. */
  def request(acceptType: MediaType): RestRequest = {
    headers.put("Accept", acceptType.toString)
    this
  }

  /** Sets path with {name} placeholders filled by variables. */
  def path(uri: String, variables: Any*): RestRequest = {
    require(uri.startsWith("/"), "uri should starts with /")
    val parts = this.parse(uri)
    var resolved = uri
    require(parts.length == variables.size, "path variable number not matched.")
    parts.zip(variables) foreach { case (k, v) =>
      resolved = Strings.replace(resolved, s"{$k}", v.toString)
    }
    this.path = resolved
    this
  }

  /** Adds query parameter. */
  def param(name: String, value: Any): RestRequest = {
    this.queryParams.addOne(name, value.toString)
    this
  }

  /** Adds query parameters. */
  def params(ps: (String, Any)*): RestRequest = {
    this.queryParams.addAll(ps.map(x => (x._1, x._2.toString)))
    this
  }

  /** Executes GET. */
  def get(): Response = {
    val request = Request.noBody
    httpUtils.get(buildURI(), request.headers(this.headers).auth(this.authorization))
  }

  /** Executes POST with body. */
  def post(body: Any, contentType: String): Response = {
    val req = Request.build(body, contentType)
    req.headers(this.headers).auth(this.authorization)
    httpUtils.invoke(buildURI(), HttpMethods.POST, req)
  }

  /** Executes POST with JSON body. */
  def postJson(json: Any): Response = {
    val load = Request.asJson(json)
    this.post(load.body, MediaTypes.json.toString)
  }

  /** Executes POST with form-urlencoded body. */
  def postForm(params: (String, Any)*): Response = {
    val load = Request.asForm(params)
    this.post(load, load.contentType)
  }

  /** Executes DELETE. */
  def delete(): Response = {
    val load = Request.build("", "application/json")
    load.headers(this.headers).auth(this.authorization)
    httpUtils.delete(buildURI(), load)
  }

  /** Executes PUT with body. */
  def put(body: Any, contentType: String): Response = {
    val req = Request.build(body, contentType)
    req.headers(this.headers).auth(this.authorization)
    httpUtils.invoke(buildURI(), HttpMethods.PUT, req)
  }

  /** Executes PUT with JSON body. */
  def putJson(json: Any): Response = {
    val load = Request.asJson(json)
    this.put(load.body, MediaTypes.json.toString)
  }

  /** Executes PUT with form-urlencoded body. */
  def putForm(params: (String, Any)*): Response = {
    val load = Request.asForm(params)
    this.put(load.body, load.contentType)
  }

  protected[ws] def buildURI(): String = {
    val hasParam = path.indexOf("?") > 0
    val url = target + path
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
    sb.mkString
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
