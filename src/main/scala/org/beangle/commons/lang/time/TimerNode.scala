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

package org.beangle.commons.lang.time

import scala.collection.mutable

/** Timer node for profiling (resource name, start time, children).
 *
 * @author chaostone
 * @since 3.0.0
 */
@SerialVersionUID(-6180672043920208784L)
class TimerNode(val resource: String, var startTime: Long) extends Serializable {

  /** Child timer nodes. */
  var children = new mutable.ListBuffer[TimerNode]

  /** Elapsed milliseconds (set by end()). */
  var totalTime: Long = _

  /** Records the start time for this node.
   *
   * @param startTime start timestamp (millis)
   */
  def start(startTime: Long): Unit =
    this.startTime = startTime

  /** Ends timing and returns elapsed milliseconds.
   *
   * @return elapsed time since start
   */
  def end(): Long = {
    this.totalTime = System.currentTimeMillis() - startTime
    this.totalTime
  }

  /** Returns a formatted string for this node and its children.
   *
   * @return tree-style string with resource names and durations
   */
  def getPrintable: String = getPrintable("")

  protected def getPrintable(indent: String): String = {
    val buffer = new StringBuilder()
    buffer.append(indent)
    buffer.append("[" + totalTime + "ms] - " + resource)
    for (child <- children)
      buffer.append('\n').append(child.getPrintable(indent + "  "))
    buffer.toString
  }
}
