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

package org.beangle.commons.net.http

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.time.DateFormats
import org.beangle.commons.lang.{Charsets, Strings}

import java.io.*
import java.net.*
import java.net.HttpURLConnection.*
import java.net.http.*
import java.time.Duration

/** HTTP client and request/response helpers. */
object HttpUtils {

  /** Default [[HttpUtils]] instance. */
  val Default = new HttpUtils(Https.createDefaultClient(), 0)

  private var clientIdx = 0
  private val statusMap = Map(
    HTTP_OK -> "OK",
    HTTP_FORBIDDEN -> "Access denied!",
    HTTP_NOT_FOUND -> "Not Found",
    HTTP_UNAUTHORIZED -> "Access denied")

  /** Session-scoped [[HttpUtils]] with in-memory [[java.net.CookieManager]] on the
   * [[java.net.http.HttpClient]] (`Https.createClient(trustAll, true)`).
   *
   * @param trustAll if true, trust all SSL certificates (first argument to `Https.createClient`)
   */
  def session(trustAll: Boolean, timeout: Duration): HttpUtils = {
    withClient(Https.createClient(trustAll, true, timeout))
  }

  /** Wraps an existing [[java.net.http.HttpClient]].
   *
   * For example from [[Https.createDefaultClient]], [[Https.createClient]], or your own builder.
   *
   * @param client
   * share one instance when the client keeps session state (cookie jar, connection pool, etc.)
   * @return HttpUtils instance
   */
  def withClient(client: HttpClient): HttpUtils = {
    clientIdx += 1
    new HttpUtils(client, clientIdx)
  }

  /** Converts HTTP status code to human-readable string.
   *
   * @param httpCode the status code
   * @return status message
   */
  def toString(httpCode: Int): String = {
    statusMap.getOrElse(httpCode, String.valueOf(httpCode))
  }

  /** Returns true if the URI is reachable (HEAD returns 200).
   *
   * @param uri the URI to check
   * @return true if alive
   */
  def isAlive(uri: String): Boolean = {
    Default.access(uri).isOk
  }

  /** Performs HEAD request to get resource status.
   *
   * @param uri the URI
   * @return ResourceStatus with code, length, lastModified
   */
  def access(uri: String): ResourceStatus = {
    Default.access(uri)
  }

  /** Performs GET request with no body.
   *
   * @param uri the URI
   * @return Response
   */
  def get(uri: String): Response = {
    Default.get(uri, Request.noBody)
  }

  /** Performs GET request with headers/auth.
   *
   * @param uri     the URI
   * @param request the request (headers, auth)
   * @return Response
   */
  def get(uri: String, request: Request): Response = {
    Default.get(uri, request)
  }

  /** Performs POST with body and content-type.
   *
   * @param uri         the URI
   * @param body        the request body
   * @param contentType the Content-Type header
   * @return Response
   */
  def post(uri: String, body: AnyRef, contentType: String): Response = {
    Default.invoke(uri, HttpMethods.POST, Request.build(body, contentType))
  }

  /** Performs POST request.
   *
   * @param uri     the URI
   * @param request the request (body, headers)
   * @return Response
   */
  def post(uri: String, request: Request): Response = {
    Default.invoke(uri, HttpMethods.POST, request)
  }

  /** Performs PUT request.
   *
   * @param uri     the URI
   * @param request the request
   * @return Response
   */
  def put(uri: String, request: Request): Response = {
    Default.invoke(uri, HttpMethods.PUT, request)
  }

  /** Performs DELETE request.
   *
   * @param uri     the URI
   * @param request the request
   * @return Response
   */
  def delete(uri: String, request: Request): Response = {
    Default.delete(uri, request)
  }

  /** Invokes POST or PUT with request body.
   *
   * @param uri     the URI
   * @param method  POST or PUT
   * @param request the request
   * @return Response
   */
  def invoke(uri: String, method: String, request: Request): Response = {
    Default.invoke(uri, method, request)
  }

  /** Downloads the URI to the given file.
   *
   * @param uri      the URI to download
   * @param location the destination file
   * @return true if successful
   */
  def download(uri: String, location: File): Boolean = {
    Default.download(uri, location)
  }
}

class HttpUtils private(private val client: HttpClient, id: Int) {

  private var userAgent = Https.defaultUserAgent

  def setUserAgent(ua: String): Unit = {
    if (id == 0) throw new IllegalArgumentException("Cannot change default HttpUtils user agent,using config")
    this.userAgent = ua
  }

  /** Performs HEAD request to get resource status.
   *
   * @param uri the URI
   * @return ResourceStatus
   */
  def access(uri: String): ResourceStatus = {
    val ouri = URI.create(uri)
    try {
      val request = HttpRequest.newBuilder(ouri)
        .method(HttpMethods.HEAD, HttpRequest.BodyPublishers.noBody())
        .build()
      val response = client.send(request, HttpResponse.BodyHandlers.discarding())
      val rc = response.statusCode()
      rc match {
        case HTTP_OK =>
          val supportRange = "bytes" == response.headers().firstValue("Accept-Ranges").orElse(null)
          val contentLength = response.headers().firstValueAsLong("Content-Length").orElse(0L)
          val lm = response.headers().firstValue("Last-Modified").orElse("")
          val lastModified = if Strings.isBlank(lm) then 0 else DateFormats.Http.parse(lm).toInstant.toEpochMilli
          ResourceStatus(rc, ouri, contentLength, lastModified, supportRange)
        case _ => ResourceStatus(rc, ouri, -1, -1, supportRange = false)
      }
    } catch {
      case e: Exception => ResourceStatus(HTTP_NOT_FOUND, ouri, -1, -1, false)
    }
  }

  /** Performs GET request.
   *
   * @param uri     the URI
   * @param request the request
   * @return Response
   */
  def get(uri: String, request: Request): Response = {
    try {
      val builder = HttpRequest.newBuilder(URI.create(uri)).method(HttpMethods.GET, HttpRequest.BodyPublishers.noBody())
      val res = client.send(setup(builder, request), HttpResponse.BodyHandlers.ofByteArray())
      Response(res.statusCode(), res.body(), convertHeaders(res.headers()))
    } catch {
      case e: Exception => error(uri, e)
    }
  }

  /** Performs DELETE request.
   *
   * @param uri     the URI
   * @param request the request
   * @return Response
   */
  def delete(uri: String, request: Request): Response = {
    try {
      val builder = HttpRequest.newBuilder(URI.create(uri)).method(HttpMethods.DELETE, HttpRequest.BodyPublishers.noBody())
      val res = client.send(setup(builder, request), HttpResponse.BodyHandlers.ofByteArray())
      Response(res.statusCode(), res.body(), convertHeaders(res.headers()))
    } catch {
      case e: Exception => error(uri, e)
    }
  }

  /** Performs POST request.
   *
   * @param uri     the URI
   * @param request the request
   * @return Response
   */
  def post(uri: String, request: Request): Response = {
    invoke(uri, HttpMethods.POST, request)
  }

  /** Performs PUT request.
   *
   * @param uri     the URI
   * @param request the request
   * @return Response
   */
  def put(uri: String, request: Request): Response = {
    invoke(uri, HttpMethods.PUT, request)
  }

  /** Invokes POST or PUT.
   *
   * @param uri     the URI
   * @param method  POST or PUT
   * @param request the request
   * @return Response
   */
  def invoke(uri: String, method: String, request: Request): Response = {
    require(HttpMethods.POST == method || HttpMethods.PUT == method, "Only support POST or PUT in invoke")
    try {
      val builder = HttpRequest.newBuilder(URI.create(uri))
      request.body.map {
        case ba: Array[Byte] => HttpRequest.BodyPublishers.ofByteArray(ba)
        case is: InputStream => HttpRequest.BodyPublishers.ofInputStream(() => is)
        case v => HttpRequest.BodyPublishers.ofString(v.toString, Charsets.UTF_8)
      }.foreach(publisher => builder.method(method, publisher))

      val res = client.send(setup(builder, request), HttpResponse.BodyHandlers.ofByteArray())
      Response(res.statusCode(), res.body(), convertHeaders(res.headers()))
    } catch {
      case e: Exception => error(uri, e)
    }
  }

  /** Downloads the URI to the given file.
   *
   * @param uri      the URI
   * @param location the destination file
   * @return true if successful
   */
  def download(uri: String, location: File): Boolean = {
    try {
      val request = HttpRequest.newBuilder(URI.create(uri))
        .method(HttpMethods.GET, HttpRequest.BodyPublishers.noBody())
        .build()
      val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
      if (response.statusCode() == HTTP_OK) {
        location.getParentFile.mkdirs()
        val file = new File(location.toString + ".part")
        file.delete()
        val output = new FileOutputStream(file)
        IOs.copy(response.body(), output)
        // close stream before renaming
        IOs.close(output)
        if (location.exists()) location.delete()
        file.renameTo(location)
        true
      } else {
        false
      }
    } catch {
      case e: Throwable => false
    }
  }

  private[this] def error(uri: String, e: Exception): Response = {
    Response(404, e.getMessage, Map.empty)
  }

  private def setup(builder: HttpRequest.Builder, request: Request): HttpRequest = {
    request.headers foreach { (k, v) => builder.header(k, v.toString) }
    if (request.cookies.nonEmpty) {
      builder.header("Cookie", request.cookies.map { case (n, v) => s"$n=$v" }.mkString("; "))
    }
    request.authorization foreach { auth => builder.header("Authorization", auth) }
    if (request.body.nonEmpty && Strings.isNotBlank(request.contentType)) {
      builder.header("Content-Type", request.contentType)
    }
    val headers = request.headers.keys.map(_.toLowerCase).toSet
    if (!headers.contains("user-agent")) {
      builder.header("User-Agent", userAgent)
    }
    builder.build()
  }

  private def convertHeaders(headers: HttpHeaders): Map[String, List[String]] = {
    import scala.jdk.javaapi.CollectionConverters.asScala
    asScala(headers.map()).map(x => (x._1, asScala(x._2).toList)).toMap
  }
}
