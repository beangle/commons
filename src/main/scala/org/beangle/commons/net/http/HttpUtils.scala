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

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.codec.binary.Base64
import org.beangle.commons.io.IOs
import org.beangle.commons.json.Json
import org.beangle.commons.lang.Charsets

import java.io.*
import java.net.*
import java.net.HttpURLConnection.*
import java.nio.charset.Charset
import scala.collection.mutable

object HttpUtils {

  private val Timeout = 10 * 1000

  private val statusMap = Map(
    HTTP_OK -> "OK",
    HTTP_FORBIDDEN -> "Access denied!",
    HTTP_NOT_FOUND -> "Not Found",
    HTTP_UNAUTHORIZED -> "Access denied")

  final class Payload(val body: Any, val contentType: String) {
    protected[http] val headers = new mutable.HashMap[String, Any]
    protected[http] var authorization: Option[String] = None

    def addHeader(name: String, value: String): Payload = {
      headers.put(name, value)
      this
    }

    def authBasic(username: String, password: String): Payload = {
      this.authorization = Some("Basic " + Base64.encode(s"$username:$password".getBytes))
      this
    }

    def authBearer(token: String): Payload = {
      this.authorization = Some(s"Bearer $token")
      this
    }

    def accept(acceptType: String): Payload = {
      headers.put("Accept", acceptType)
      this
    }
  }

  def toString(httpCode: Int): String = {
    statusMap.getOrElse(httpCode, String.valueOf(httpCode))
  }

  def isAlive(url: String): Boolean = {
    access(url).isOk
  }

  def access(url: String): ResourceStatus = {
    access(URI.create(url).toURL)
  }

  def access(url: URL): ResourceStatus = {
    try {
      val hc = followRedirect(url.openConnection(), HttpMethods.HEAD)
      val rc = hc.getResponseCode
      rc match {
        case HTTP_OK =>
          val supportRange = "bytes" == hc.getHeaderField("Accept-Ranges")
          ResourceStatus(rc, hc.getURL, hc.getHeaderFieldLong("Content-Length", 0), hc.getLastModified, supportRange)
        case _ => ResourceStatus(rc, hc.getURL, -1, -1, supportRange = false)
      }
    } catch {
      case _: Exception => ResourceStatus(HTTP_NOT_FOUND, url, -1, -1, false)
    }
  }

  @scala.annotation.tailrec
  def followRedirect(c: URLConnection, method: String): HttpURLConnection = {
    val conn = c.asInstanceOf[HttpURLConnection]
    conn.setInstanceFollowRedirects(false)
    requestBy(conn, method)
    Https.noverify(conn)
    val rc = conn.getResponseCode
    rc match {
      case HTTP_OK => conn
      case HTTP_MOVED_TEMP | HTTP_MOVED_PERM =>
        val newLoc = conn.getHeaderField("location")
        followRedirect(URI.create(newLoc).toURL.openConnection, method)
      case _ => conn
    }
  }

  def getData(urlString: String, method: String = HttpMethods.GET): Response = {
    getData(URI.create(urlString).toURL, method, None)
  }

  def getData(url: URL, method: String, username: String, password: String): Response = {
    getData(url, method, Some({ x =>
      x.addRequestProperty("Authorization", "Basic " + Base64.encode(s"$username:$password".getBytes))
    }))
  }

  def getData(url: URL, method: String, f: Option[HttpURLConnection => Unit]): Response = {
    var conn: HttpURLConnection = null
    try {
      conn = url.openConnection().asInstanceOf[HttpURLConnection]
      conn.setDoOutput(false)
      conn.setUseCaches(false)
      f foreach (x => x(conn))
      conn = followRedirect(conn, method)
      if (conn.getResponseCode == HTTP_OK) {
        val bos = new ByteArrayOutputStream
        IOs.copy(conn.getInputStream, bos)
        Response(conn.getResponseCode, bos.toByteArray)
      } else
        Response(conn.getResponseCode, conn.getResponseMessage)
    } catch {
      case e: Exception => error(url, e)
    } finally
      if (null != conn) conn.disconnect()
  }

  def getText(urlString: String): Response = {
    getText(URI.create(urlString).toURL, HttpMethods.GET, Charsets.UTF_8, None)
  }

  def getText(url: URL, method: String, encoding: Charset): Response = {
    getText(url, method, encoding, None)
  }

  def getText(url: URL, method: String, encoding: Charset, username: String, password: String): Response = {
    getText(url, method, encoding, Some({ x =>
      x.addRequestProperty("Authorization", "Basic " + Base64.encode(s"$username:$password".getBytes))
    }))
  }

  def getText(url: URL, method: String, encoding: Charset, f: Option[(URLConnection) => Unit]): Response = {
    var conn: HttpURLConnection = null
    var in: BufferedReader = null
    try {
      conn = url.openConnection().asInstanceOf[HttpURLConnection]
      conn.setDoOutput(false)
      conn.setUseCaches(false)
      f foreach (x => x(conn))
      conn = followRedirect(conn, method)
      if conn.getResponseCode == HTTP_OK then
        in = new BufferedReader(new InputStreamReader(conn.getInputStream, encoding))
        var line: String = in.readLine()
        val sb = new StringBuilder(255)
        while (line != null) {
          sb.append(line)
          line = in.readLine()
          if (null != line) sb.append("\n")
        }
        Response(HTTP_OK, sb.toString)
      else
        Response(conn.getResponseCode, conn.getResponseMessage)
    } catch {
      case e: Exception => error(url, e)
    } finally {
      if (null != in) in.close()
      if (null != conn) conn.disconnect()
    }
  }

  def invoke(url: URL, body: AnyRef, contentType: String): Response = {
    invoke(url, payload(body, contentType))
  }

  @deprecated("using invoke by payload", "4.7.1")
  def invoke(url: URL, body: AnyRef, contentType: String, username: String, password: String): Response = {
    invoke(url, payload(body, contentType).authBasic(username, password))
  }

  def invoke(url: URL, body: AnyRef, contentType: String, f: Option[URLConnection => Unit]): Response = {
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    Https.noverify(conn)
    conn.setDoOutput(true)
    requestBy(conn, HttpMethods.POST)
    conn.setRequestProperty("Content-Type", contentType)
    f foreach (x => x(conn))

    val os = conn.getOutputStream
    payload(body, contentType).body match {
      case ba: Array[Byte] => os.write(ba)
      case v => writeBody(os, v.toString)
    }
    os.close() //don't forget to close the OutputStream
    try {
      conn.connect()
      val bos = new ByteArrayOutputStream
      IOs.copy(conn.getInputStream, bos)
      Response(conn.getResponseCode, bos.toByteArray)
    } catch {
      case e: Exception => error(url, e)
    } finally
      if (null != conn) conn.disconnect()
  }

  def invoke(url: URL, payload: Payload): Response = {
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    Https.noverify(conn)
    conn.setDoOutput(true)
    requestBy(conn, HttpMethods.POST)
    payload.headers foreach { (k, v) => conn.addRequestProperty(k, v.toString) }
    payload.authorization foreach { auth => conn.setRequestProperty("Authorization", auth) }
    conn.setRequestProperty("Content-Type", payload.contentType)

    val os = conn.getOutputStream
    payload.body match {
      case ba: Array[Byte] => os.write(ba)
      case v => writeBody(os, v.toString)
    }
    os.close() //don't forget to close the OutputStream
    try {
      conn.connect()
      val bos = new ByteArrayOutputStream
      IOs.copy(conn.getInputStream, bos)
      Response(conn.getResponseCode, bos.toByteArray)
    } catch {
      case e: Exception => error(url, e)
    } finally
      if (null != conn) conn.disconnect()
  }

  def download(c: URLConnection, location: File): Boolean = {
    val conn = followRedirect(c, "GET")
    location.getParentFile.mkdirs()
    var input: InputStream = null
    var output: OutputStream = null
    try {
      val file = new File(location.toString + ".part")
      file.delete()
      val buffer = Array.ofDim[Byte](1024 * 4)
      input = conn.getInputStream
      output = new FileOutputStream(file)
      var n = input.read(buffer)
      while (-1 != n) {
        output.write(buffer, 0, n)
        n = input.read(buffer)
      }
      //先关闭文件读写，再改名
      IOs.close(input, output)
      input = null
      output = null
      if (location.exists()) location.delete()
      file.renameTo(location)
      true
    } catch {
      case e: Throwable => false
    }
    finally {
      IOs.close(input, output)
    }
  }

  private[this] def error(url: URL, e: Exception): Response = {
    Response(404, e.getMessage)
  }

  private def requestBy(conn: HttpURLConnection, method: String): Unit = {
    conn.setConnectTimeout(Timeout)
    conn.setReadTimeout(Timeout)
    conn.setRequestMethod(method)
  }

  /** Convert any data to payload
   *
   * @param data        body data
   * @param contentType content-type string
   * @return
   */
  def payload(data: Any, contentType: String): Payload = {
    if contentType.startsWith("application/json") then asJson(data)
    else if contentType.startsWith("application/x-www-form-urlencoded") then asForm(data)
    else new Payload(data, contentType)
  }

  /** Convert data to Json Payload
   *
   * @param data body
   * @return
   */
  def asJson(data: Any): Payload = {
    require(null != data, "body cannot be empty")
    val newBody = data match {
      case params: collection.Map[_, _] => Json.toJson(params)
      case list: Iterable[_] => Json.toJson(list)
      case str: String => str
      case _ => Json.toLiteral(data)
    }
    new Payload(newBody, MediaTypes.ApplicationJson.toString)
  }

  /** Convert data to Payload
   *
   * @param data body
   * @return
   */
  def asForm(data: Any): Payload = {
    require(null != data, "body cannot be empty")
    val newBody = data match {
      case params: collection.Map[_, _] => encodeTuple(params)
      case list: Iterable[_] =>
        if (list.nonEmpty) {
          if list.head.isInstanceOf[(_, _)] then encodeTuple(list.asInstanceOf[Iterable[(_, _)]]) else ""
        } else {
          ""
        }
      case str: String => str
      case _ => data.toString
    }
    new Payload(newBody, MediaTypes.ApplicationFormUrlencoded.toString)
  }

  private def writeBody(os: OutputStream, payload: String): Unit = {
    val osw = new OutputStreamWriter(os, "UTF-8")
    osw.write(payload)
    osw.flush()
    osw.close()
  }

  private def encodeTuple(params: Iterable[(_, _)]): String = {
    val paramBuffer = new mutable.ArrayBuffer[String]
    params foreach { e =>
      if (e._2 != null) {
        paramBuffer.addOne(e._1.toString + "=" + URLEncoder.encode(e._2.toString, Charsets.UTF_8))
      }
    }
    paramBuffer.mkString("&")
  }

}
