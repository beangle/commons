package org.beangle.commons.file.text

import java.io.{ File, FileInputStream, FileOutputStream }

import org.beangle.commons.collection.Collections
import org.beangle.commons.io.IOs
import org.beangle.commons.io.Files./
import org.beangle.commons.lang.{ Charsets, Strings }
import org.beangle.commons.activation.MimeTypes

object Formater {
  val LF = "\n"
  val CRLF = "\r\n"

  def format(formater: Formater, dir: File, ext: Option[String]) {
    if (dir.isFile) {
      ext match {
        case Some(f) =>
          if (dir.getName.endsWith(f)) formater.format(dir)
        case None => {
          val fileExt = Strings.substringAfterLast(dir.getName, ".")

          MimeTypes.getMimeType(fileExt) foreach { m =>
            if (m.getPrimaryType == "text" || fileExt == "xml") formater.format(dir)
          }
        }
      }
    } else {
      dir.list() foreach { childName =>
        format(formater, new File(dir.getAbsolutePath + / + childName), ext)
      }
    }
  }

}

trait Formater {

  def format(str: String): String

  def format(file: File): Unit = {
    val content = IOs.readString(new FileInputStream(file))
    val rs = format(content)
    val fos = new FileOutputStream(file)
    IOs.write(rs, fos, Charsets.UTF_8)
    IOs.close(fos)
  }
}

class FormaterBuilder {
  var tablength = 2
  var eof = Formater.LF
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
    assert(1 <= tablength && tablength <= 8, "tablength should in [1,8]")
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
    assert(eof == Formater.LF || eof == Formater.CRLF)
    this.eof = eof
    this
  }

  def build(): Formater = {
    val processors = Collections.newBuffer[LineProcessor]
    if (tab2space) processors += new Tab2Space(tablength)
    if (trimTrailingWhiteSpace) processors += TrimTrailingWhiteSpace
    new DefaultFormater(eof, processors.toList, fixLast)
  }
}

class DefaultFormater(val eof: String = Formater.LF, lineProcessors: List[LineProcessor], val fixLast: Boolean) extends Formater {

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