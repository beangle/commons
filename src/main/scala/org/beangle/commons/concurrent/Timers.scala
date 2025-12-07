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

import java.util.{Timer, TimerTask}

object Timers {

  def start(name: String, intervalSeconds: Int, tasks: Runnable*): Unit = {
    val daemon = new Timers(tasks)
    new Timer(s"$name", true).schedule(daemon,
      new java.util.Date(System.currentTimeMillis), intervalSeconds * 1000)
  }

  def setTimeout(delaySecond: Int, f: () => Unit): Unit = {
    val t = new Timer()
    t.schedule(new TimerTask() {
      override def run(): Unit = {
        f()
        t.cancel()
      }
    }, delaySecond * 1000)
  }
}

class Timers(tasks: collection.Seq[Runnable]) extends TimerTask {
  override def run(): Unit = {
    tasks foreach { task =>
      try {
        task.run()
      } catch {
        case e: Throwable => e.printStackTrace()
      }
    }
  }
}
