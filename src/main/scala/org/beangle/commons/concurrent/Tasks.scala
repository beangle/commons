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

/** Start multiple thread simultaneously
 */
object Tasks {

  /** Start collection of runnable
   */
  def start(runnables: Iterator[Runnable]): Unit = {
    val tasks = new collection.mutable.ListBuffer[Thread]
    for (runnable <- runnables) {
      val thread = new Thread(runnable)
      tasks += thread
      thread.start()
    }
    tasks.foreach { task =>
      try
        task.join()
      catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
  }

  /** Start runnable multiple
   */
  def start(runnable: Runnable, threadPoolSize: Int, name: String = null): Unit = {
    val tasks = new collection.mutable.ListBuffer[Thread]
    var index = 0;
    while (index < threadPoolSize) {
      val thread = if (null == name) new Thread(runnable) else new Thread(runnable, name + index)
      tasks += thread
      index += 1
      thread.start()
    }
    tasks.foreach { task =>
      try
        task.join()
      catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
  }
}
