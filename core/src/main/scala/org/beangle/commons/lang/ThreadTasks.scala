/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang

/**
 * Start multiple thread simultaneously
 */
object ThreadTasks {

  /**
   * Start collection of runnable
   */
  def start(runables: Iterator[Runnable]) {
    val tasks = new collection.mutable.ListBuffer[Thread]
    for (runable <- runables) {
      val thread = new Thread(runable)
      tasks += thread
      thread.start()
    }
    for (task <- tasks) {
      try {
        task.join()
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
  }

  /**
   * Start runnable multiple
   */
  def start(runable: Runnable, threadPoolSize: Int, name: String = null) {
    val tasks = new collection.mutable.ListBuffer[Thread]
    var index = 0;
    while (index < threadPoolSize) {
      val thread = if (null == name) new Thread(runable) else new Thread(runable, name + index)
      tasks += thread
      index += 1
      thread.start()
    }
    for (task <- tasks) {
      try {
        task.join()
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
  }
}