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

/** Parallel job processing with thread pool. */
object Workers {

  /** Processes payloads in parallel using multiple threads.
   *
   * @param payloads       the items to process
   * @param job            the function to apply to each item
   * @param threadPoolSize number of threads (0 = availableProcessors)
   */
  def work[T](payloads: Iterable[T], job: T => Unit, threadPoolSize: Int = 0): Unit = {
    val loads = new LinkedBlockingQueue[T]
    import scala.jdk.CollectionConverters.*
    loads.addAll(payloads.toSeq.asJava)

    val threads = if threadPoolSize <= 0 then Runtime.getRuntime.availableProcessors else threadPoolSize
    Tasks.start(new Work(loads, job), threads)
  }

  class Work[T](payloads: LinkedBlockingQueue[T], job: T => Unit) extends Runnable {

    def run(): Unit = {
      while (!payloads.isEmpty) {
        try {
          val pk = payloads.poll()
          if (null != pk) job(pk)
        } catch {
          case e: Exception =>
        }
      }
    }
  }
}
