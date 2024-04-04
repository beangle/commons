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

package org.beangle.commons.file.text

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.collection.Collections
import org.beangle.commons.io.Files./
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{Charsets, Strings}

import java.io.{File, FileInputStream, FileOutputStream}

object Formatter {
  val LF = "\n"
  val CRLF = "\r\n"

  def format(formatter: Formatter, dir: File, ext: Option[String]): Unit =
    if dir.isFile then
      ext match {
        case Some(f) =>
          if (dir.getName.endsWith(f)) formatter.format(dir)
        case None =>
          val fileExt = Strings.substringAfterLast(dir.getName, ".")
          MediaTypes.get(fileExt) foreach { m =>
            if (m.primaryType == "text" || fileExt == "xml") formatter.format(dir)
          }
      }
    else
      dir.list() foreach { childName =>
        format(formatter, new File(dir.getAbsolutePath + / + childName), ext)
      }
}

trait Formatter {

  def format(str: String): String

  def format(file: File): Unit = {
    val content = IOs.readString(new FileInputStream(file))
    val rs = format(content)
    val fos = new FileOutputStream(file)
    IOs.write(rs, fos, Charsets.UTF_8)
    IOs.close(fos)
  }
}

class FormatterBuilder {
  var tablength = 2
  var eof = Formatter.LF
  private var tab2space: Boolean = _
  private var trimTrailingWhiteSpace: Boolean = _
  private var fixLast: Boolean = _

  def enableTrimTrailingWhiteSpace(): this.type = {
    trimTrailingWhiteSpace = true
    this
  }

  def disableTrimTrailingWhiteSpace(): this.type = {
    trimTrailingWhiteSpace = false
    this
  }

  def enableTab2space(tablength: Int): this.type = {
    assert(1 <= tablength && tablength <= 8, "tab length should in [1,8]")
    this.tablength = tablength
    tab2space = true
    this
  }

  def disableTab2space(): this.type = {
    tab2space = false
    this
  }

  def insertFinalNewline(): this.type = {
    fixLast = true
    this
  }

  def fixcrlf(eof: String): this.type = {
    assert(eof == Formatter.LF || eof == Formatter.CRLF)
    this.eof = eof
    this
  }

  def build(): Formatter = {
    val processors = Collections.newBuffer[LineProcessor]
    if (tab2space) processors += new Tab2Space(tablength)
    if (trimTrailingWhiteSpace) processors += TrimTrailingWhiteSpace
    new DefaultFormatter(eof, processors.toList, fixLast)
  }
}

class DefaultFormatter(val eof: String = Formatter.LF, lineProcessors: List[LineProcessor], val fixLast: Boolean) extends Formatter {

  def format(str: String): String = {
    var fixlf = Strings.replace(str, "\r", "")
    var smaller = Strings.replace(fixlf, "\n\n\n", "\n\n")
    while (smaller.length < fixlf.length) {
      fixlf = smaller
      smaller = Strings.replace(smaller, "\n\n\n", "\n\n")
    }
    val lines = fixlf.split('\n')
    var i = 0
    while (i < lines.length) {
      var line = lines(i)
      lineProcessors foreach { p =>
        line = p.process(line)
      }
      lines(i) = line
      i += 1
    }
    join(lines, eof, fixLast)
  }

  private def join(seq: Array[String], eof: String, fixLast: Boolean): String = {
    val seqLen = seq.length
    val aim = new StringBuilder()
    (0 until seq.length) foreach { i =>
      if (i > 0) aim.append(eof)
      aim.append(seq(i))
    }
    if (fixLast) aim.append(eof)
    aim.toString
  }
}
