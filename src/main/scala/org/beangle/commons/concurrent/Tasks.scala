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

package org.beangle.commons.concurrent

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.atomic.AtomicInteger

/** Runs multiple tasks in parallel and waits for completion. */
object Tasks {

  @deprecated("using run ", since = "6.0.8")
  def start(runnable: Runnable, repeat: Int, name: String = null): Unit = {
    run(runnable, repeat, None)
  }

  @deprecated("using run ", since = "6.0.8")
  def start(runnables: Iterator[Runnable]): Unit = {
    run(runnables, None)
  }

  /** Starts each runnable in a new thread and joins all.
   *
   * @param runnables the tasks to run
   */
  def run(runnables: Iterator[Runnable], handler: Option[UncaughtExceptionHandler]): Int = {
    val tasks = new collection.mutable.ListBuffer[Thread]
    val reporter = new TaskReporter(handler)
    for (runnable <- runnables) {
      val thread = Thread.ofVirtual().uncaughtExceptionHandler(reporter).start(runnable)
      tasks += thread
    }
    tasks.foreach { task =>
      try
        task.join()
      catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
    reporter.failed.get
  }

  /** Starts the same runnable in multiple threads and joins all.
   *
   * @param runnable the task to run
   * @param repeat   number of threads
   * @return failed executed count
   */
  def run(runnable: Runnable, repeat: Int, handler: Option[UncaughtExceptionHandler]): Int = {
    val tasks = new collection.mutable.ListBuffer[Thread]
    var index = 0
    val reporter = TaskReporter(handler)
    while (index < repeat) {
      val thread = Thread.ofVirtual().uncaughtExceptionHandler(reporter).start(runnable)
      tasks += thread
      index += 1
    }
    tasks.foreach { task =>
      try
        task.join()
      catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
    reporter.failed.get()
  }

  /** Collection fail count and invoke customHandler
   *
   * @param customHandler custom handler
   */
  class TaskReporter(customHandler: Option[UncaughtExceptionHandler]) extends UncaughtExceptionHandler {
    var failed: AtomicInteger = new AtomicInteger(0)

    def uncaughtException(thread: Thread, throwable: Throwable): Unit = {
      failed.incrementAndGet()
      customHandler match {
        case None => throwable.printStackTrace()
        case Some(h) =>
          try {
            h.uncaughtException(thread, throwable)
          } catch {
            case e: Throwable => e.printStackTrace()
          }
      }
    }
  }

  /** Swallow ncaughtException
   */
  object MuteHandler extends UncaughtExceptionHandler {
    def uncaughtException(thread: Thread, throwable: Throwable): Unit = {

    }
  }
}
