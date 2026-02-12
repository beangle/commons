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

package org.beangle.commons.io

import org.beangle.commons.lang.Charsets.UTF_8

import java.io.*
import java.net.URL
import java.nio.charset.Charset
import java.util as ju

/** I/O utilities (copy, read/write bytes and lines). */
object IOs {

  private val defaultBufferSize = 1024 * 4

  private val eof = -1

  /** Copy bytes from a `InputStream` to an `OutputStream`.
   *
   * @param input  the `InputStream` to read from
   * @param output the `OutputStream` to write to
   * @return the number of bytes copied
   * @since 3.1
   */
  def copy(input: InputStream, output: OutputStream): Long = {
    val buffer = new Array[Byte](defaultBufferSize)
    var count = 0
    var n = input.read(buffer)
    while (eof != n) {
      output.write(buffer, 0, n)
      count += n
      n = input.read(buffer)
    }
    close(input)
    count
  }

  /** Reads all bytes from the input stream and returns them as an array.
   *
   * @param input the input stream
   * @return the byte array; stream is closed after read
   */
  def readBytes(input: InputStream): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    copy(input, baos)
    baos.toByteArray
  }

  /** Writes the string to the output stream using the given charset.
   *
   * @param data    the string to write
   * @param output  the output stream
   * @param charset the charset (null for default)
   */
  def write(data: String, output: OutputStream, charset: Charset = null): Unit = {
    if data != null then
      if charset == null then output.write(data.getBytes())
      else output.write(data.getBytes(charset))
  }

  /** Copy chars from a `Reader` to a `Writer`.
   *
   * @param input  the `Reader` to read from
   * @param output the `Writer` to write to
   * @return the number of characters copied
   * @since 3.1
   */
  def copy(input: Reader, output: Writer): Long = {
    val buffer = new Array[Char](defaultBufferSize)
    var count = 0
    var n = input.read(buffer)
    while (eof != n) {
      output.write(buffer, 0, n)
      count += n
      n = input.read(buffer)
    }
    close(input)
    count
  }

  /** Reads the Reader as a list of lines.
   *
   * @param input the Reader
   * @return one string per line; stream is closed after read
   */
  def readLines(input: Reader): List[String] = {
    val reader = toBufferedReader(input)
    val list = new collection.mutable.ListBuffer[String]
    var line = reader.readLine()
    while (line != null) {
      list += line
      line = reader.readLine()
    }
    close(input)
    list.toList
  }

  /** Reads the InputStream as a string using the given charset.
   *
   * @param input   the input stream
   * @param charset the charset
   * @return the string; stream is closed after read
   */
  def readString(input: InputStream, charset: Charset = UTF_8): String = {
    if (null == input) ""
    else
      try {
        val sw = new StringBuilderWriter(16)
        copy(new InputStreamReader(input, charset), sw)
        sw.toString
      } finally
        close(input)
  }

  /** Reads key=value properties from the URL.
   *
   * @param url the properties URL
   * @return map of key -> value
   */
  def readProperties(url: URL): Map[String, String] = {
    if null == url then Map.empty
    else readProperties(url.openStream())
  }

  /** Reads key=value properties from the InputStream.
   *
   * @param input   the input stream
   * @param charset the charset
   * @return map of key -> value; stream is closed after read
   */
  def readProperties(input: InputStream, charset: Charset = UTF_8): Map[String, String] = {
    if null == input then Map.empty
    else
      val texts = new collection.mutable.HashMap[String, String]
      val reader = new LineNumberReader(new InputStreamReader(input, charset))
      var line: String = reader.readLine
      while (null != line) {
        val index = line.indexOf('=')
        if (index > 0) texts.put(line.substring(0, index).trim(), line.substring(index + 1).trim())
        line = reader.readLine()
      }
      close(input)
      texts.toMap
  }

  /** Reads Java Properties format from the URL.
   *
   * @param url the properties URL
   * @return map of key -> value
   */
  def readJavaProperties(url: URL): Map[String, String] = {
    if null == url then Map.empty
    else readJavaProperties(url.openStream())
  }

  /** Reads Java Properties format from the InputStream.
   *
   * @param input the input stream
   * @return map of key -> value; stream is closed after read
   */
  def readJavaProperties(input: InputStream): Map[String, String] = {
    if null == input then Map.empty
    else
      val properties = new ju.Properties()
      properties.load(input)
      close(input)
      import scala.jdk.CollectionConverters.*
      properties.asScala.toMap
  }

  /** Reads the InputStream as lines using the given charset.
   *
   * @param input   the input stream
   * @param charset the charset
   * @return list of lines; stream is closed after read
   */
  def readLines(input: InputStream, charset: Charset = UTF_8): List[String] = {
    readLines(new InputStreamReader(input, charset))
  }

  /** Closes the given AutoCloseables, swallowing any exceptions.
   *
   * @param objs the resources to close
   */
  def close(objs: AutoCloseable*): Unit = {
    objs foreach { obj =>
      try if (obj != null) obj.close()
      catch case _: Exception => {}
    }
  }

  /** Executes the function with the resource, then closes it.
   *
   * @param res  the AutoCloseable resource
   * @param func the function to run
   * @return the function result
   */
  def using[T <: AutoCloseable, R](res: T)(func: T => R): R = {
    try func(res)
    finally
      if res != null then res.close()
  }

  /** Concatenates multiple InputStreams into a single SequenceInputStream.
   *
   * @param ins the input streams in order
   * @return the combined stream
   */
  def pipeline(ins: Iterable[InputStream]): InputStream = {
    new SequenceInputStream(scala.jdk.javaapi.CollectionConverters.asJavaEnumeration(ins.iterator))
  }

  private def toBufferedReader(reader: Reader): BufferedReader = {
    reader match {
      case reader1: BufferedReader => reader1
      case _ => new BufferedReader(reader)
    }
  }
}
