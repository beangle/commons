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
import org.beangle.commons.lang.{Charsets, Strings}

import java.nio.file.{Files, Path, Paths}
import java.util.regex.Pattern

/**
 * Window Command Utility
 */
object WinCmd extends Shell(Charsets.GBK) {

  /** Execute a command
   *
   * @param args command and args
   * @return (exit code,output)
   */
  override def exec(arg: String): (Int, collection.Seq[String]) = {
    require(null != arg, "Need command")
    // if absolute path, run directly; otherwise wrap in cmd /c
    val args = Collections.newBuffer[String]
    if (!Shell.isFile(arg)) {
      args.addOne("cmd")
      args.addOne("/c")
    }
    args.addOne(arg)
    execute(args.toSeq: _*)
  }

  override def find(exename: String): Option[Path] = {
    checkExeName(exename)
    findExeByWhere(exename).orElse(findExeFromRegistry(exename))
  }

  override def killall(exename: String): Int = {
    checkExeName(exename)
    // /NH hides table header for simpler parsing
    val rs = execute("tasklist", "/FI", s"IMAGENAME eq $exename", "/NH")
    val pids = rs._2.filter(_.startsWith(exename)).map(x => Strings.split(x, " ").apply(1).toInt)
    kill(pids.toSeq: _*)
    pids.size
  }

  override def kill(pids: Int*): Unit = {
    for (pid <- pids) {
      execute("taskkill", "/F", "/T", "/PID", pid.toString)
    }
  }

  private def findExeByWhere(exename: String): Option[Path] = {
    val rs = execute("where", exename)
    if (rs._1 > 0) {
      None
    } else {
      val outputs = rs._2
      val goods = outputs.filter { path =>
        path.endsWith(exename) && !path.contains("Temp") && !path.contains("临时")
      }.map(p => Paths.get(p))

      goods.filter(p => Files.isRegularFile(p)).map(_.toRealPath()).headOption
    }
  }

  /** Reads exename's App Paths from Windows registry to get full path.
   *
   * @param exename the executable name (e.g. "cmd.exe")
   * @return the full path, or None if not found
   */
  private def findExeFromRegistry(exename: String): Option[Path] = {
    val lines = exec("reg query \"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\" + exename + "\" /ve")
    val filePattern = Strings.replace(exename, ".", "\\.")
    val pattern = Pattern.compile("REG_SZ\\s+(.+" + filePattern + ")", Pattern.CASE_INSENSITIVE)
    if (lines._1 > 0) {
      None
    } else {
      val paths = Collections.newBuffer[Path]
      for (line <- lines._2) {
        val matcher = pattern.matcher(line.trim)
        if (matcher.find) {
          val path = matcher.group(1).trim
          val r = Paths.get(path)
          if Files.isRegularFile(r) then paths.addOne(r)
        }
      }
      paths.headOption
    }
  }

}
