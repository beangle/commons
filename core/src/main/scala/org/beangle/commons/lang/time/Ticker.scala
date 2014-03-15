/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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

object Ticker {

  /**
   * A ticker that reads the current time using {@link System#nanoTime}.
   */
  def systemTicker(): Ticker = defaultTicker

  private val defaultTicker = new SystemTicker
}

/**
 * Time source generator
 *
 * @author chaostone
 * @since 3.0.0
 */
abstract class Ticker() {

  /**
   * Returns the number of nanoseconds elapsed
   */
  def read(): Long
}

class SystemTicker extends Ticker {
  override def read(): Long = System.nanoTime()
}
