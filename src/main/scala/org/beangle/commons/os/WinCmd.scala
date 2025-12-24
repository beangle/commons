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
import org.beangle.commons.lang.{Processes, Strings}

import java.io.{BufferedReader, File, InputStreamReader}
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.{Executors, TimeUnit}
import java.util.regex.Pattern

/**
 * Window Command Utility
 */
object WinCmd {

  /** Execute a command
   *
   * @param args command and args
   * @return (exit code,output)
   */
  def exec(args: String*): (Int, collection.Seq[String]) = {
    require(args.nonEmpty, "Need command")
    val newArgs = Collections.newBuffer[String]
    //如果是个绝对地址，则直接执行，不用放在cmd环境中执行。
    if (!isFile(args.head)) {
      newArgs.addOne("cmd")
      newArgs.addOne("/c")
    }
    newArgs.addAll(args)
    val processBuilder = new ProcessBuilder(newArgs.toSeq: _*)
    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()
    val contents = Collections.newBuffer[String]

    val executor = Executors.newSingleThreadExecutor
    executor.submit(() => {
      val inputStream = process.getInputStream
      val reader = new BufferedReader(new InputStreamReader(inputStream, "GBK"))
      val maxLine = 10000
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

  def find(exename: String): Option[Path] = {
    findExeByWhere(exename).orElse(findExeFromRegistry(exename))
  }

  private def findExeByWhere(exename: String): Option[Path] = {
    val rs = exec("where", exename)
    if (rs._1 > 0) {
      None
    } else {
      val outputs = rs._2
      val goods = outputs.filter { path =>
        path.endsWith(exename) && !(path.contains("Temp")) && !path.contains("临时")
      }
      val file = Paths.get(goods.head)
      Some(file).filter(x => Files.isRegularFile(x))
    }
  }

  /**
   * 读取注册表中 exename 的 App Paths 项，获取完整路径
   *
   * @return exename 完整路径（null 表示未找到）
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

  /** 判断一个命令是否是个可执行文件
   *
   * @param command cmd
   */
  private def isFile(command: String): Boolean = {
    (command.contains('/') || command.contains('\\')) && new File(command).exists()
  }
}
