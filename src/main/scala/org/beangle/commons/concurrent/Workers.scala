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

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/** Parallel job processing with thread pool. */
object Workers {

  @deprecated("using work(payload,thread,job)", since = "6.0.8")
  def work[T](payloads: Iterable[T], job: T => Unit, threadPoolSize: Int = 0): Unit = {
    workOn(payloads, threadPoolSize)(job)
  }

  /** Processes payloads in parallel using multiple threads.
   *
   * @param payloads    the items to process
   * @param job         the function to apply to each item
   * @param threadCount number of threads (0 = availableProcessors)
   */
  def workOn[T](payloads: Iterable[T], threadCount: Int)(job: T => Unit): Int = {
    val loads = new LinkedBlockingQueue[T]
    import scala.jdk.CollectionConverters.*
    loads.addAll(payloads.toSeq.asJava)

    val threads = if threadCount <= 0 then Runtime.getRuntime.availableProcessors else threadCount
    val work = new Work(loads, job)
    Tasks.run(work, threads, None)
    work.failed.get
  }

  class Work[T](payloads: LinkedBlockingQueue[T], job: T => Unit) extends Runnable {
    var failed: AtomicInteger = new AtomicInteger(0)

    def run(): Unit = {
      while (!payloads.isEmpty) {
        try {
          val pk = payloads.poll()
          if (null != pk) job(pk)
        } catch {
          case e: Exception =>
            failed.incrementAndGet()
            e.printStackTrace()
        }
      }
    }
  }

}
