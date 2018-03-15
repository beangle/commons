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
package org.beangle.commons.io

import java.io.{ BufferedReader, Closeable, IOException, InputStream, InputStreamReader, LineNumberReader, OutputStream, Reader, Writer }
import java.net.URL
import java.nio.charset.Charset
import java.{ util => ju }

import org.beangle.commons.lang.Charsets.UTF_8
import org.beangle.commons.lang.time.Stopwatch

object IOs {

  private val defaultBufferSize = 1024 * 4

  private val eof = -1

  /**
   * Copy bytes from a <code>InputStream</code> to an <code>OutputStream</code>.
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
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
    close(input)
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
          System.err.println("load " + url + " error", e)
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
   * Read Java key value properties by url
   */
  def readJavaProperties(url: URL): Map[String, String] = {
    if (null == url) Map.empty
    else {
      try {
        readJavaProperties(url.openStream())
      } catch {
        case e: Exception => {
          System.err.println("load " + url + " error", e)
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
      collection.JavaConverters.propertiesAsScalaMap(properties).toMap
    }
  }

  def readLines(input: InputStream, charset: Charset = UTF_8): List[String] = {
    readLines(new InputStreamReader(input, charset))
  }

  /**
   * Close many objects quitely.
   * swallow any exception.
   */
  def close(objs: AutoCloseable*) {
    objs foreach { obj =>
      try {
        if (obj != null) obj.close()
      } catch {
        case ioe: Exception =>
      }
    }
  }

  def using[T <: AutoCloseable, R](res: T)(func: T => R): R = {
    try {
      func(res)
    } finally {
      if (res != null)
        res.close()
    }
  }

  private def toBufferedReader(reader: Reader): BufferedReader = {
    if (reader.isInstanceOf[BufferedReader]) reader.asInstanceOf[BufferedReader] else new BufferedReader(reader)
  }
}
