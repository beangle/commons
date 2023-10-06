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

package org.beangle.commons.lang

import org.beangle.commons.io.IOs
import org.beangle.commons.logging.Logging

import java.io.{BufferedReader, InputStreamReader}
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CompletableFuture, TimeUnit, TimeoutException}
import java.util.regex.{Matcher, Pattern}

object Processes extends Logging {

  val DefaultWaitSeconds = 60

  def launch(program: String, args: collection.Seq[String]): Process = {
    launch(program, args, _.inheritIO())
  }

  def launch(program: String, args: collection.Seq[String], f: ProcessBuilder => Unit): Process = {
    val arguments = new java.util.ArrayList[String]
    arguments.add(program)
    import scala.jdk.CollectionConverters.*
    arguments.addAll(args.asJava)
    val pb = new ProcessBuilder().command(arguments)
    f(pb)
    pb.start()
  }

  def isExecutable(binaryPath: String): Boolean = {
    isExecutable(Paths.get(binaryPath))
  }

  /** 按照环境变量或者可选路径查找可执行命令
   *
   * @param envName
   * @param alternatives
   * @return
   */
  def find(envName: String, alternatives: collection.Seq[String]): Option[Path] = {
    val path = System.getenv(envName)
    if (null == path) {
      var result: Path = null
      var i = 0
      while (result == null && i < alternatives.length) {
        val pi = alternatives(i)
        if Processes.isExecutable(pi) then result = Paths.get(pi).toAbsolutePath
        i += 1
      }
      Option(result)
    } else {
      if Processes.isExecutable(path) then Some(Paths.get(path).toAbsolutePath)
      else
        logger.error(s"${envName}:${path} is not an executable file.")
        None
    }
  }

  /** 关闭进程
   *
   * @param process
   * @param waitSeconds
   */
  def close(process: Process, waitSeconds: Int = DefaultWaitSeconds): Int = {
    if (process != null && process.isAlive) {
      process.destroy()
      try {
        if (!process.waitFor(waitSeconds, TimeUnit.SECONDS)) {
          process.destroyForcibly()
          process.waitFor(waitSeconds, TimeUnit.SECONDS)
        }
      } catch {
        case e: InterruptedException => process.destroyForcibly()
      }
    }
    if (null != process) process.exitValue() else 0
  }

  def grep(process: Process, pattern: Pattern, waitSeconds: Int): Option[Matcher] = {
    val result = new CompletableFuture[Matcher]
    val output = new AtomicReference[String]("")
    val readLine = new Runnable:
      override def run(): Unit = {
        var sb = new StringBuilder
        var reader: BufferedReader = null
        try {
          reader = new BufferedReader(new InputStreamReader(process.getInputStream))
          var line = reader.readLine()
          while (line != null && !result.isDone) {
            val matcher = pattern.matcher(line)
            if (matcher.find()) {
              result.complete(matcher)
              sb = null
              output.set(null)
              line = null
            } else {
              if (sb.nonEmpty) sb.append(System.lineSeparator)
              sb.append(line)
              output.set(sb.toString)
              line = reader.readLine()
            }
          }
        } catch {
          case e: Exception => if !result.isDone then result.completeExceptionally(e)
        } finally IOs.close(reader)
      }

    val readLineThread = new Thread(readLine)
    readLineThread.setName("read-line-thread")
    readLineThread.start()

    try Option(result.get(waitSeconds, TimeUnit.SECONDS))
    catch {
      case e: TimeoutException =>
        close(readLineThread)
        logger.debug("Failed while waiting for starting: Timeout expired! output: " + output.get)
        None
      case e: Exception =>
        close(readLineThread)
        logger.error("Failed while waiting for starting.", e)
        None
    }
  }

  private def close(thread: Thread): Unit = {
    try thread.join(TimeUnit.SECONDS.toMillis(5))
    catch {
      case e: InterruptedException =>
    }
  }

  private def isExecutable(path: Path) = Files.isRegularFile(path) && Files.isReadable(path) && Files.isExecutable(path)

}
