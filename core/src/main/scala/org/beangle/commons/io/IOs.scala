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
package org.beangle.commons.io

import java.io.{ BufferedReader, Closeable, IOException, InputStream, InputStreamReader, LineNumberReader, OutputStream, Reader, Writer }
import java.net.URL
import java.nio.charset.Charset
import java.{ util => ju }

import org.beangle.commons.lang.Charsets.UTF_8
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging

object IOs extends Logging {

  private val defaultBufferSize = 1024 * 4

  private val eof = -1

  /**
   * Copy bytes from a <code>InputStream</code> to an <code>OutputStream</code>.
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
   * @return the number of bytes copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
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
    count
  }

  def write(data: String, output: OutputStream, charset: Charset = null) {
    if (data != null) {
      if (charset == null)
        output.write(data.getBytes())
      else
        output.write(data.getBytes(charset))
    }
  }

  /**
   * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
   *
   * @param input the <code>Reader</code> to read from
   * @param output the <code>Writer</code> to write to
   * @return the number of characters copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
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
    count
  }

  /**
   * Get the contents of a <code>Reader</code> as a list of Strings,
   * one entry per line.
   */
  def readLines(input: Reader): List[String] = {
    val reader = toBufferedReader(input)
    val list = new collection.mutable.ListBuffer[String]
    var line = reader.readLine()
    while (line != null) {
      list += line
      line = reader.readLine()
    }
    list.toList
  }

  def readString(input: InputStream, charset: Charset = UTF_8): String = {
    try {
      val sw = new StringBuilderWriter(16)
      copy(new InputStreamReader(input, charset), sw)
      sw.toString
    } finally {
      close(input)
    }
  }

  /**
   * Read key value properties
   */
  def readProperties(url: URL): Map[String, String] = {
    if (null == url) Map.empty
    else {
      try {
        readProperties(url.openStream())
      } catch {
        case e: Exception => {
          error("load " + url + " error", e)
          Map.empty
        }
      }
    }
  }

  /**
   * Read key value properties
   */
  def readProperties(input: InputStream, charset: Charset = UTF_8): Map[String, String] = {
    if (null == input) Map.empty
    else {
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
  }
  /**
   * Read key value properties
   * Group by Uppercased key,and default group
   */
  def readBundles(input: InputStream, charset: Charset = UTF_8): Map[String, Map[String, String]] = {
    if (null == input) Map.empty
    else {
      val defaults = ""
      val texts = new collection.mutable.HashMap[String, collection.mutable.HashMap[String, String]]
      val reader = new LineNumberReader(new InputStreamReader(input, charset))
      var line: String = reader.readLine
      while (null != line) {
        val index = line.indexOf('=')
        if (index > 0 && index != line.length - 1) {
          val key = line.substring(0, index).trim()
          val value = line.substring(index + 1).trim()
          if (Character.isUpperCase(key.charAt(0))) {
            val dotIdx = key.indexOf('.')
            if (-1 == dotIdx) {
              texts.getOrElseUpdate(defaults, new collection.mutable.HashMap[String, String]).put(key, value)
            } else {
              texts.getOrElseUpdate(key.substring(0, dotIdx), new collection.mutable.HashMap[String, String]).put(key.substring(dotIdx + 1), value)
            }
          } else {
            texts.getOrElseUpdate(defaults, new collection.mutable.HashMap[String, String]).put(key, value)
          }
        }
        line = reader.readLine()
      }
      close(input)
      val results = texts.map { case (name, values) => (name, values.toMap) }
      results.toMap
    }
  }
  /**
   * Read Java key value properties by url
   */
  def readJavaProperties(url: URL): Map[String, String] = {
    if (null == url) Map.empty
    else {
      try {
        readJavaProperties(url.openStream())
      } catch {
        case e: Exception => {
          error("load " + url + " error", e)
          Map.empty
        }
      }
    }
  }
  /**
   * Read java key value properties
   */
  def readJavaProperties(input: InputStream): Map[String, String] = {
    if (null == input) Map.empty
    else {
      val properties = new ju.Properties()
      properties.load(input)
      close(input)
      collection.JavaConversions.propertiesAsScalaMap(properties).toMap
    }
  }

  def readLines(input: InputStream, charset: Charset = UTF_8): List[String] = readLines(new InputStreamReader(input, charset))

  /**
   * Close many objects quitely.
   * TODO Support AutoCloseable when beangle based on jdk 1.7
   */
  def close(objs: Closeable*) {
    objs foreach { obj =>
      try {
        if (obj != null) obj.close()
      } catch {
        case ioe: IOException =>
      }
    }
  }

  private def toBufferedReader(reader: Reader): BufferedReader = {
    if (reader.isInstanceOf[BufferedReader]) reader.asInstanceOf[BufferedReader] else new BufferedReader(reader)
  }
}
