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

object HttpUtils {

  val Default = new HttpUtils(createClient(java.lang.Boolean.getBoolean("beangle.https.trust-all")))

  private val statusMap = Map(
    HTTP_OK -> "OK",
    HTTP_FORBIDDEN -> "Access denied!",
    HTTP_NOT_FOUND -> "Not Found",
    HTTP_UNAUTHORIZED -> "Access denied")

  def createClient(trustAll: Boolean = false): HttpClient = {
    if (trustAll) {
      Https.createTrustAllClient()
    } else {
      Https.createDefaultClient()
    }
  }

  def withClient(client: HttpClient): HttpUtils = {
    new HttpUtils(client)
  }

  def toString(httpCode: Int): String = {
    statusMap.getOrElse(httpCode, String.valueOf(httpCode))
  }

  def isAlive(uri: String): Boolean = {
    Default.access(uri).isOk
  }

  def access(uri: String): ResourceStatus = {
    Default.access(uri)
  }

  def get(uri: String): Response = {
    Default.get(uri, Request.noBody)
  }

  def get(uri: String, request: Request): Response = {
    Default.get(uri, request)
  }

  def post(uri: String, body: AnyRef, contentType: String): Response = {
    Default.invoke(uri, HttpMethods.POST, Request.build(body, contentType))
  }

  def post(uri: String, request: Request): Response = {
    Default.invoke(uri, HttpMethods.POST, request)
  }

  def put(uri: String, request: Request): Response = {
    Default.invoke(uri, HttpMethods.PUT, request)
  }

  def delete(uri: String, request: Request): Response = {
    Default.delete(uri, request)
  }

  def invoke(uri: String, method: String, request: Request): Response = {
    Default.invoke(uri, method, request)
  }

  def download(uri: String, location: File): Boolean = {
    Default.download(uri, location)
  }
}

class HttpUtils private(private val client: HttpClient) {

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

  def get(uri: String, request: Request): Response = {
    try {
      val builder = HttpRequest.newBuilder(URI.create(uri)).method(HttpMethods.GET, HttpRequest.BodyPublishers.noBody())
      val res = client.send(setup(builder, request), HttpResponse.BodyHandlers.ofByteArray())
      Response(res.statusCode(), res.body())
    } catch {
      case e: Exception => error(uri, e)
    }
  }

  def delete(uri: String, request: Request): Response = {
    try {
      val builder = HttpRequest.newBuilder(URI.create(uri)).method(HttpMethods.DELETE, HttpRequest.BodyPublishers.noBody())
      val res = client.send(setup(builder, request), HttpResponse.BodyHandlers.ofByteArray())
      Response(res.statusCode(), res.body())
    } catch {
      case e: Exception => error(uri, e)
    }
  }

  def post(uri: String, request: Request): Response = {
    invoke(uri, HttpMethods.POST, request)
  }

  def put(uri: String, request: Request): Response = {
    invoke(uri, HttpMethods.PUT, request)
  }

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
      Response(res.statusCode(), res.body())
    } catch {
      case e: Exception => error(uri, e)
    }
  }

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
        //先关闭文件读写，再改名
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
    Response(404, e.getMessage)
  }

  private def setup(builder: HttpRequest.Builder, request: Request): HttpRequest = {
    request.headers foreach { (k, v) => builder.header(k, v.toString) }
    request.authorization foreach { auth => builder.header("Authorization", auth) }
    if (request.body.nonEmpty && Strings.isNotBlank(request.contentType)) {
      builder.header("Content-Type", request.contentType)
    }
    builder.build()
  }
}
