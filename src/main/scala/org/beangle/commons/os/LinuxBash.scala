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
import org.beangle.commons.lang.Charsets

import java.nio.file.{Files, Path, Paths}

object LinuxBash extends Shell(Charsets.UTF_8) {

  override def find(exename: String): Option[Path] = {
    checkExeName(exename)
    val rs = exec("where", exename)
    if (rs._1 > 0) {
      None
    } else {
      val goods = rs._2.filter(path => path.endsWith(exename)).map(p => Paths.get(p))
      goods.filter(p => Files.isRegularFile(p)).map(_.toRealPath()).headOption
    }
  }

  /** Execute a command
   *
   * @param args command and args
   * @return (exit code,output)
   */
  override def exec(args: String*): (Int, collection.Seq[String]) = {
    require(args.nonEmpty, "Need command")
    //如果是个绝对地址，则直接执行，不用放在cmd环境中执行。
    val newArgs = Collections.newBuffer[String]
    if (!Shell.isFile(args.head)) {
      newArgs.addOne("bash")
      newArgs.addOne("-c")
    }
    newArgs.addAll(args)
    execute(newArgs.toSeq: _*)
  }

  override def killall(exename: String): Int = {
    checkExeName(exename)
    val rs = exec("ps -ef | grep " + exename + " |grep -v grep | wc -l")
    if (rs._1 == 0) {
      val s = rs._2.mkString.toInt
      if (s > 0) {
        exec("ps -ef | grep " + exename + " | grep -v grep | awk '{print \"kill -9 \"$2}' | sh")
      }
      s
    } else {
      0
    }
  }

  override def kill(pids: Int*): Unit = {
    exec("kill", "-9", pids.mkString(" "))
  }
}
