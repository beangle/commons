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

import org.beangle.commons.bean.Disposable
import org.beangle.commons.concurrent.Sidecar.Worker

import java.util as ju
import java.util.concurrent.ArrayBlockingQueue

/** Async queue with worker thread. */
object Sidecar {

  /** Worker thread that processes items from the sidecar queue. */
  class Worker[T](car: Sidecar[T], job: T => Unit) extends Thread {
    var stopped: Boolean = false

    override def run(): Unit = {
      while (!stopped) {
        try {
          val elements = new ju.ArrayList[T]
          val e0 = car.queue.take()
          elements.add(e0)
          car.queue.drainTo(elements) //dump to elements
          val iter = elements.iterator()
          while (iter.hasNext) {
            job(iter.next())
          }
        } catch {
          case _: InterruptedException => stopped = true
        }
      }
    }
  }
}

/** Background queue processor. Items offered are processed by a worker thread.
 *
 * @param name     worker thread name
 * @param job      the function to apply to each item
 * @param capacity queue capacity
 */
class Sidecar[T](name: String, job: T => Unit, capacity: Int = 512) extends Disposable {
  private val queue = new ArrayBlockingQueue[T](capacity)
  private val worker: Worker[T] = new Worker[T](this, job)

  worker.setDaemon(true)
  worker.setName(name)
  worker.start()

  /** Adds an item to the queue for processing.
   *
   * @param t the item to process
   */
  def offer(t: T): Unit = {
    queue.offer(t)
  }

  override def destroy(): Unit = {
    worker.interrupt()
  }
}
