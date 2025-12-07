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

package org.beangle.commons.text.replace

import org.beangle.commons.io.Files
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.reflect.Reflections

import java.io.{File, FileOutputStream, FilenameFilter, OutputStreamWriter}
import java.nio.charset.Charset
import scala.collection.mutable

object BatchReplaceMain {

  /** Usage:BatchReplaceMain dir patternfile encoding
   */
  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      log("using BatchReplaceMain dir patternfile encoding")
      return
    }
    val dir = args(0)
    if (!new File(dir).exists()) {
      log(dir + " not a valid file or directory")
      return
    }
    val properties = args(1)
    if (!new File(properties).exists())
      log(properties + " not valid file or directory")
    var charset: Charset = null
    if (args.length >= 3)
      charset = Charset.forName(args(2))
    val lines = Files.readLines(new File(properties))
    val profiles = new mutable.HashMap[String, List[Replacer]]

    var profileName = ""
    for (line <- lines if Strings.isNotEmpty(line))
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
        val replacer: Replacer = if (older == "replacer") Reflections.newInstance(newer) else new PatternReplacer(older, newer)
        profiles.put(profileName, replacer :: profiles(profileName))
      }
    replaceFile(dir, profiles.toMap, charset)
  }

  /** replaceFile.
   */
  def replaceFile(fileName: String, profiles: Map[String, List[Replacer]], charset: Charset): Unit = {
    val file = new File(fileName)
    if (file.isFile && !file.isHidden) {
      val replacers = profiles.get(Strings.substringAfterLast(fileName, ".")).orNull
      if (null == replacers) return
      log("processing " + fileName)
      var filecontent = Files.readString(file, charset)
      filecontent = Replacer.process(filecontent, replacers)
      writeToFile(filecontent, fileName, charset)
    } else {
      val subFiles = file.list(new FilenameFilter() {
        def accept(dir: File, name: String): Boolean = {
          if dir.isDirectory then
            true
          else
            var matched = false
            for (key <- profiles.keySet if !matched) {
              matched = name.endsWith(key)
            }
            matched
        }
      })
      if (null != subFiles)
        for (i <- 0 until subFiles.length) replaceFile(fileName + '/' + subFiles(i), profiles, charset)
    }
  }

  /** writeToFile.
   */
  def writeToFile(str: String, fileName: String, charset: Charset): Unit = {
    val writer =
      if null == charset then new OutputStreamWriter(new FileOutputStream(fileName))
      else new OutputStreamWriter(new FileOutputStream(fileName), charset.name())
    writer.write(str)
    writer.close()
  }

  private def log(msg: String): Unit = {
    println(msg)
  }
}
