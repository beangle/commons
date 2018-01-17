/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
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
package org.beangle.commons.lang.time

object TimerTrace {

  protected var curStack: ThreadLocal[TimerStack] = new ThreadLocal[TimerStack]()

  /**
   * System property that controls whether this timer should be used or not. Set to "true" activates
   * the timer. Set to "false" to disactivate.
   */
  val ACTIVATE_PROPERTY = "beangle.profile.activate"

  /**
   * System property that controls the min time, that if exceeded will cause a log (at INFO level)
   * to be created.
   */
  val MIN_TIME = "beangle.profile.mintime"

  /**
   * Initialized in a static block, it can be changed at runtime by calling setActive(...)
   */
  var active: Boolean = "true".equalsIgnoreCase(System.getProperty(ACTIVATE_PROPERTY))

  /**
   * Get the min time for this profiling, it searches for a System property
   * 'beangle.profile.mintime' and default to 0.
   */
  private var mintime: Int = _

  try {
    mintime = Integer.parseInt(System.getProperty(MIN_TIME, "0"))
  } catch {
    case e: NumberFormatException =>
  }

  /**
   * Create and start a performance profiling with the <code>name</code> given. Deal with
   * profile hierarchy automatically, so caller don't have to be concern about it.
   *
   * @param name profile name
   */
  def start(name: String) {
    if (!active) return
    val root = new TimerNode(name, System.currentTimeMillis())
    val stack = curStack.get.asInstanceOf[TimerStack]
    if (null == stack) curStack.set(new TimerStack(root)) else stack.push(root)
  }

  /**
   * End a preformance profiling with the <code>name</code> given. Deal with
   * profile hierarchy automatically, so caller don't have to be concern about it.
   */
  def end() {
    if (!active) return
    val stack = curStack.get
    if (null == stack) return
    val currentNode = stack.pop()
    if (currentNode != null) {
      val parent = stack.peek()
      val total = currentNode.end()
      if (parent == null) {
        printTimes(currentNode)
        curStack.set(null)
      } else {
        if (total > mintime) parent.children += currentNode
      }
    }
  }

  /**
   * Do a log (at INFO level) of the time taken for this particular profiling.
   *
   * @param currentTimer profiling timer bean
   */
  private def printTimes(currentTimer: TimerNode) {
    println(currentTimer.getPrintable)
  }

  /**
   * Get the min time for this profiling, it searches for a System property
   * 'beangle.profile.mintime' and default to 0.
   *
   * @return long
   */
  def getMinTime(): Int = mintime

  /**
   * Change mintime
   *
   * @param mintime
   */
  def setMinTime(mintime: Int) {
    System.setProperty(MIN_TIME, String.valueOf(mintime))
    TimerTrace.mintime = mintime
  }

  /**
   * Turn profiling on or off.
   *
   * @param active
   */
  def setActive(active: Boolean) {
    if (active) System.setProperty(ACTIVATE_PROPERTY, "true") else System.clearProperty(ACTIVATE_PROPERTY)
    TimerTrace.active = active
  }

  def isActive = active

  def clear() {
    curStack.set(null)
  }

}

