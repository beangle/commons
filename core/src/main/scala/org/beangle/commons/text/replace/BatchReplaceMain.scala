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
package org.beangle.commons.text.replace

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import org.beangle.commons.io.Files
import org.beangle.commons.lang.Strings
import scala.collection.mutable
import org.beangle.commons.lang.ClassLoaders

object BatchReplaceMain {

  /**
   * Usage:BatchReplaceMain dir patternfile encoding
   */
  def main(args: Array[String]) {
    if (args.length < 2) {
      println("using BatchReplaceMain dir patternfile encoding")
      return
    }
    val dir = args(0)
    if (!new File(dir).exists()) {
      println(dir + " not a valid file or directory")
      return
    }
    val properties = args(1)
    if (!new File(properties).exists()) {
      println(properties + " not valid file or directory")
    }
    var charset: Charset = null
    if (args.length >= 3) {
      charset = Charset.forName(args(2))
    }
    val lines = Files.readLines(new File(properties))
    val profiles = new mutable.HashMap[String, List[Replacer]]

    var profileName = ""
    for (line <- lines if Strings.isNotEmpty(line)) {
      if (-1 == line.indexOf('=')) {
        profileName = line
        profiles.put(line, Nil)
      } else {
        val replacedline = Strings.replace(line, "\\=", "~~~~")
        var older = Strings.replace(Strings.substringBefore(replacedline, "="), "~~~~", "=").trim()
        var newer = Strings.replace(Strings.substringAfter(replacedline, "="), "~~~~", "=").trim()
        older = Strings.replace(older, "\\n", "\n")
        older = Strings.replace(older, "\\t", "\t")
        newer = Strings.replace(newer, "\\n", "\n")
        newer = Strings.replace(newer, "\\t", "\t")
        val replacer:Replacer = if (older == "replacer") ClassLoaders.newInstance(newer) else new PatternReplacer(older, newer)
        profiles.put(profileName, replacer :: profiles(profileName))
      }
    }
    replaceFile(dir, profiles.toMap, charset)
  }

  /**
   * replaceFile.
   */
  def replaceFile(fileName: String, profiles: Map[String, List[Replacer]], charset: Charset) {
    val file = new File(fileName)
    if (file.isFile && !file.isHidden) {
      val replacers = profiles.get(Strings.substringAfterLast(fileName, ".")).orNull
      if (null == replacers) return
      println("processing " + fileName)
      var filecontent = Files.readString(file, charset)
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
