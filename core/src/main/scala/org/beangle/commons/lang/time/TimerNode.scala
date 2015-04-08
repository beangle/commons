/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
import scala.collection.mutable

/**
 * Timer Node in stack
 *
 * @author chaostone
 * @since 3.0.0
 */
@SerialVersionUID(-6180672043920208784L)
class TimerNode(val resource: String, var startTime: Long) extends Serializable {

  var children = new mutable.ListBuffer[TimerNode]

  var totalTime: Long = _

  def start(startTime: Long) {
    this.startTime = startTime
  }

  def end(): Long = {
    this.totalTime = System.currentTimeMillis() - startTime
    this.totalTime
  }

  /**
   * Get a formatted string representing all the methods that took longer than a specified time.
   */
  def getPrintable(): String = getPrintable("")

  protected def getPrintable(indent: String): String = {
    val buffer = new StringBuilder()
    buffer.append(indent)
    buffer.append("[" + totalTime + "ms] - " + resource)
    for (child <- children) {
      buffer.append('\n').append(child.getPrintable(indent + "  "))
    }
    buffer.toString
  }
}
