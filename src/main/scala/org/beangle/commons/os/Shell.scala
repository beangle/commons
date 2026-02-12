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

package org.beangle.commons.os

import org.beangle.commons.collection.Collections
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{Charsets, Processes}
import org.beangle.commons.os.Shell.isValidFileName

import java.io.{BufferedReader, File, InputStreamReader}
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.concurrent.{Executors, TimeUnit}
import java.util.regex.Pattern

/** Shell command helpers (isFile, isValidFileName). */
object Shell {

  /** Returns true if the command is a path to an existing file.
   *
   * @param command the command string to check
   * @return true if it is a file path that exists
   */
  def isFile(command: String): Boolean = {
    (command.contains('/') || command.contains('\\')) && new File(command).exists()
  }

  /** Returns true if the name is a safe executable filename (no path chars).
   *
   * @param exename the name to validate
   * @return true if valid
   */
  def isValidFileName(exename: String): Boolean = {
    ImageNamePattern.matcher(exename).matches &&
      !DangerousChars.exists(x => exename.indexOf(x) > -1)
  }

  private val ImageNamePattern = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._-]{0,50}$")

  private val DangerousChars = Array(";", "&", "|", ">", "<", "(", ")", "*", "?", "/", "\\", "`", "$", "\"", "'")
}

/** OS shell command utility. */
abstract class Shell(charset: Charset = Charsets.UTF_8) {

  /** Maximum lines to read from command output. */
  var maxLine: Int = 10000

  /** Execute a command
   *
   * @param args command and args
   * @return (exit code,output)
   */
  def execute(args: String*): (Int, collection.Seq[String]) = {
    require(args.nonEmpty, "Need command")
    val processBuilder = new ProcessBuilder(args.toSeq: _*)
    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()
    val contents = Collections.newBuffer[String]

    val executor = Executors.newSingleThreadExecutor
    executor.submit(() => {
      val inputStream = process.getInputStream
      val reader = new BufferedReader(new InputStreamReader(inputStream, charset))
      try {
        var line = reader.readLine
        var lineNo = 1
        while (line != null && lineNo <= maxLine) {
          val path = line.trim
          if (path.nonEmpty) contents.addOne(path)
          line = reader.readLine
          lineNo += 1
        }
        if (lineNo >= maxLine && null != line) {
          contents.addOne("....")
        }
      } catch {
        case e: Exception => contents.addOne(e.getMessage)
      } finally {
        IOs.close(inputStream, reader)
      }
    })

    if (!process.waitFor(5, TimeUnit.SECONDS)) {
      Processes.close(process)
    }
    executor.shutdown()
    (process.exitValue(), contents)
  }

  final def checkExeName(exename: String): Unit = {
    require(isValidFileName(exename), s"${exename} is not valid command file")
  }

  def exec(args: String): (Int, collection.Seq[String])

  /** find a execute file real path
   *
   * @param exename filename
   * @return
   */
  def find(exename: String): Option[Path]

  /** killall process which equals exename
   *
   * @param exename image name
   */
  def killall(exename: String): Int

  /** kill process by pids
   *
   * @param pids pid
   */
  def kill(pids: Int*): Unit
}
