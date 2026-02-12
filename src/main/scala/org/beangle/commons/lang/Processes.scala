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

import java.io.{BufferedReader, InputStreamReader}
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CompletableFuture, TimeUnit, TimeoutException}
import java.util.regex.{Matcher, Pattern}

/** Process launch and output capture. */
object Processes {

  /** Default wait time for process termination (seconds). */
  val DefaultWaitSeconds = 60

  /** Launches process with inherited I/O. */
  def launch(program: String, args: collection.Seq[String]): Process = {
    launch(program, args, _.inheritIO())
  }

  /** Launches process with custom ProcessBuilder config. */
  def launch(program: String, args: collection.Seq[String], f: ProcessBuilder => Unit): Process = {
    val arguments = new java.util.ArrayList[String]
    arguments.add(program)
    import scala.jdk.CollectionConverters.*
    arguments.addAll(args.asJava)
    val pb = new ProcessBuilder().command(arguments)
    f(pb)
    pb.start()
  }

  /** Returns true if the path is a readable, executable file. */
  def isExecutable(binaryPath: String): Boolean = {
    isExecutable(Paths.get(binaryPath))
  }

  /** Finds executable path via environment variable or alternative paths.
   *
   * @param envName      the environment variable name (e.g. "JAVA_HOME")
   * @param alternatives fallback paths if env var is not set
   * @return the executable Path, or None if not found
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
      else None
    }
  }

  /** Terminates the process, waiting up to waitSeconds for it to exit.
   *
   * @param process     the process to close
   * @param waitSeconds maximum seconds to wait
   * @return the process exit value
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

  /** Reads process stdout until pattern matches; returns Some(matcher) or None. */
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
        None
      case e: Exception =>
        close(readLineThread)
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
