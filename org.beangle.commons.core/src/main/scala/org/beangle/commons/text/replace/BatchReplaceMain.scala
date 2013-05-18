/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.text.replace

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.io.Files
import org.beangle.commons.lang.Strings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
//remove if not needed
import scala.collection.JavaConversions._

class BatchReplaceMain{
  
}
object BatchReplaceMain {

  private var logger: Logger = LoggerFactory.getLogger(classOf[BatchReplaceMain])

  /**
   * Usage:BatchReplaceMain dir patternfile encoding
   */
  def main(args: Array[String]) {
    if (args.length < 2) {
      logger.info("using BatchReplaceMain dir patternfile encoding")
      return
    }
    val dir = args(0)
    if (!new File(dir).exists()) {
      logger.error("{} not a valid file or directory", dir)
      return
    }
    val properties = args(1)
    if (!new File(properties).exists()) {
      logger.info("{} not valid file or directory", properties)
    }
    var charset: Charset = null
    if (args.length >= 3) {
      charset = Charset.forName(args(2))
    }
    val lines = Files.readLines(new File(properties))
    val profiles = CollectUtils.newHashMap[String,List[Replacer]]()
    var replacers: List[Replacer] = null
    for (line <- lines) {
      if (Strings.isEmpty(line)) {
        //continue
      }
      if (-1 == line.indexOf('=')) {
        replacers = CollectUtils.newArrayList()
        profiles.put(line, replacers)
      } else {
        val replacedline = Strings.replace(line, "\\=", "~~~~")
        var older = Strings.replace(Strings.substringBefore(replacedline, "="), "~~~~", "=")
        var newer = Strings.replace(Strings.substringAfter(replacedline, "="), "~~~~", "=")
        older = Strings.replace(older, "\\n", "\n")
        older = Strings.replace(older, "\\t", "\t")
        newer = Strings.replace(newer, "\\n", "\n")
        newer = Strings.replace(newer, "\\t", "\t")
        val pair = new Replacer(older, newer)
        replacers.add(pair)
      }
    }
    replaceFile(dir, profiles, charset)
  }

  /**
   * <p>
   * replaceFile.
   * </p>
   *
   * @param fileName a {@link java.lang.String} object.
   * @param profiles a {@link java.util.Map} object.
   * @param encoding a {@link java.lang.String} object.
   * @throws java.lang.Exception if any.
   * @throws java.io.FileNotFoundException if any.
   */
  def replaceFile(fileName: String, profiles: Map[String, List[Replacer]], charset: Charset) {
    val file = new File(fileName)
    if (file.isFile && !file.isHidden) {
      val replacers = profiles.get(Strings.substringAfterLast(fileName, "."))
      if (null == replacers) {
        return
      }
      logger.info("processing {}", fileName)
      var filecontent = Files.readFileToString(file, charset)
      filecontent = Replacer.process(filecontent, replacers)
      writeToFile(filecontent, fileName, charset)
    } else {
      val subFiles = file.list(new FilenameFilter() {

        def accept(dir: File, name: String): Boolean = {
          if (dir.isDirectory) return true
          var matched = false
          for (key <- profiles.keySet) {
            matched = name.endsWith(key)
            if (matched) return true
          }
          return false
        }
      })
      if (null != subFiles) {
        for (i <- 0 until subFiles.length) {
          replaceFile(fileName + '/' + subFiles(i), profiles, charset)
        }
      }
    }
  }

  /**
   * <p>
   * writeToFile.
   * </p>
   */
  def writeToFile(str: String, fileName: String, charset: Charset) {
    var writer: OutputStreamWriter = null
    writer = if (null == charset) new OutputStreamWriter(new FileOutputStream(fileName)) else new OutputStreamWriter(new FileOutputStream(fileName), 
      charset.name())
    writer.write(str)
    writer.close()
  }
}
