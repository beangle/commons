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
import org.beangle.commons.collection.Collections
import org.beangle.commons.io.{Files, IOs}
import org.beangle.commons.json.Json
import org.beangle.commons.lang.{Charsets, Strings}

import java.io.{ByteArrayInputStream, File, FileInputStream, InputStream}
import java.net.URLEncoder
import java.util.UUID
import scala.collection.mutable

/** HTTP Request factory. */
object Request {

  /** Converts data to JSON payload (supports string, map, list).
   *
   * @param data the body data
   * @return Request with JSON content-type
   */
  def asJson(data: Any): Request = {
    require(null != data, "body cannot be empty")
    val newBody = data match {
      case params: collection.Map[_, _] => Json.toJson(params)
      case list: Iterable[_] => Json.toJson(list)
      case str: String => str
      case _ => Json.toLiteral(data)
    }
    new Request(Some(newBody), MediaTypes.json.toString)
  }

  /** Converts data to form payload (supports string, tuple, file, InputStream).
   *
   * @param data the form data
   * @return Request with form content-type
   */
  def asForm(data: Any): Request = {
    require(null != data, "body cannot be empty")
    val datas = data match {
      case t: (_, _) => Seq(t)
      case i: Iterable[_] => i.asInstanceOf[Iterable[(_, _)]]
      case s: String => return new Request(Some(s), MediaTypes.formUrlencoded.toString)
      case _ => throw new IllegalArgumentException(s"form data need to string/tuple/tuples,${data.getClass.getName} not supported")
    }
    val formFields = Collections.newBuffer[(String, Any)]
    val fileFields = Collections.newBuffer[(String, (String, InputStream))]

    datas foreach { case (k, v) =>
      v match {
        case f: File => fileFields.addOne(k.toString, (f.getName, new FileInputStream(f)))
        case f: InputStream => fileFields.addOne(k.toString, ("", f))
        case ispair: (_, _) =>
          val fileName = ispair._1.toString
          ispair._2 match {
            case f: File => fileFields.addOne(k.toString, (fileName, new FileInputStream(f)))
            case f: InputStream => fileFields.addOne(k.toString, (fileName, f))
            case _ =>
          }
        case _ => formFields.addOne((k.toString, v))
      }
    }
    if (fileFields.isEmpty) {
      new Request(Some(encodeTuples(formFields)), MediaTypes.formUrlencoded.toString)
    } else {
      val crlf = "\r\n"
      val dash2 = "--"
      val boundary = "*****" + UUID.randomUUID() + "****"

      val text = new StringBuilder
      val boundaryStart = dash2 + boundary + crlf
      formFields foreach { case (k, v) =>
        text.append(boundaryStart)
        text.append(s"""Content-Disposition: form-data; name="${k}"""").append(crlf)
        text.append(crlf) // blank line between headers and value
        text.append(v).append(crlf)
      }
      val ins = Collections.newBuffer[InputStream]
      ins.addOne(toStream(text))

      // each file part: boundary + headers + binary content
      fileFields foreach { case (name, (fileName, is)) =>
        val meta = new StringBuilder
        meta.append(boundaryStart)
        if (Strings.isNotEmpty(fileName)) {
          meta.append(s"""Content-Disposition: form-data; name="${name}"; filename="${Files.purify(fileName)}"""")
        } else {
          meta.append(s"""Content-Disposition: form-data; name="${name}"""")
        }
        meta.append(crlf)
        meta.append("Content-Type:application/octet-stream").append(crlf)
        meta.append(crlf) // blank line between headers and binary stream
        ins.addOne(toStream(meta))
        ins.addOne(is)
        ins.addOne(toStream(crlf))
      }
      ins.addOne(toStream(dash2 + boundary + dash2 + crlf))
      new Request(Some(IOs.pipeline(ins)), "multipart/form-data; boundary=" + boundary)
    }
  }

  /** Builds Request from data and content-type.
   *
   * @param data        the body data
   * @param contentType the Content-Type header
   * @return Request
   */
  def build(data: Any, contentType: String): Request = {
    if (null == data || data == None) {
      new Request(None, contentType)
    } else {
      if contentType.startsWith("application/json") then asJson(data)
      else if contentType.startsWith("application/x-www-form-urlencoded") then asForm(data)
      else new Request(Option(data), contentType)
    }
  }

  /** Returns an empty Request (no body).
   *
   * @return Request with no body
   */
  def noBody: Request = {
    new Request(None, "application/json")
  }

  private def toStream(s: String): InputStream = {
    new ByteArrayInputStream(s.getBytes(Charsets.UTF_8))
  }

  private def toStream(sb: StringBuilder): InputStream = {
    new ByteArrayInputStream(sb.toString.getBytes(Charsets.UTF_8))
  }

  private def encodeTuples(params: Iterable[(String, _)]): String = {
    val paramBuffer = new mutable.ArrayBuffer[String]
    params foreach { e =>
      if (e._2 != null) {
        paramBuffer.addOne(e._1 + "=" + URLEncoder.encode(e._2.toString, Charsets.UTF_8))
      }
    }
    paramBuffer.mkString("&")
  }
}

final class Request(val body: Option[Any], val contentType: String) {
  protected[http] val headers = new mutable.HashMap[String, Any]
  protected[http] var authorization: Option[String] = None

  /** Adds headers.
   *
   * @param kvs the name-value pairs
   * @return this for chaining
   */
  def headers(kvs: Iterable[(String, String)]): Request = {
    kvs foreach { kv => headers.put(kv._1, kv._2) }
    this
  }

  /** Adds a single header.
   *
   * @param name  the header name
   * @param value the header value
   * @return this for chaining
   */
  def header(name: String, value: String): Request = {
    headers.put(name, value)
    this
  }

  /** Sets authorization header.
   *
   * @param a optional auth value
   * @return this for chaining
   */
  def auth(a: Option[String]): Request = {
    this.authorization = a
    this
  }

  /** Sets authorization header.
   *
   * @param str the auth value
   * @return this for chaining
   */
  def auth(str: String): Request = {
    this.authorization = Some(str)
    this
  }

  /** Sets Basic auth from username and password.
   *
   * @param username the username
   * @param password the password
   * @return this for chaining
   */
  def auth(username: String, password: String): Request = {
    this.authorization = Some("Basic " + Base64.encode(s"$username:$password".getBytes(Charsets.UTF_8)))
    this
  }

  /** Sets Bearer token auth.
   *
   * @param token the token
   * @return this for chaining
   */
  def bearer(token: String): Request = {
    this.authorization = Some(s"Bearer $token")
    this
  }

  /** Sets Accept header.
   *
   * @param acceptType the Accept value
   * @return this for chaining
   */
  def request(acceptType: String): Request = {
    headers.put("Accept", acceptType)
    this
  }
}
