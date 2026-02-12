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

import org.beangle.commons.lang.time.Stopwatch.*

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.{MICROSECONDS, MILLISECONDS, NANOSECONDS, SECONDS}

/** Stopwatch formatting utilities. */
object Stopwatch {

  /** Returns a string representation of elapsed nanoseconds with given significant figures.
   *
   * @param nanos             the elapsed nanoseconds
   * @param significantDigits the number of significant digits
   * @return formatted string (e.g. "1.235 ms")
   */
  def format(nanos: Long, significantDigits: Int): String = {
    val unit = chooseUnit(nanos)
    val value = nanos.toDouble / NANOSECONDS.convert(1, unit)
    String.format("%." + significantDigits + "g %s", value.asInstanceOf[AnyRef], abbreviate(unit).asInstanceOf[AnyRef])
  }

  private def chooseUnit(nanos: Long): TimeUnit = {
    if (SECONDS.convert(nanos, NANOSECONDS) > 0) return SECONDS
    if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) return MILLISECONDS
    if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) return MICROSECONDS
    NANOSECONDS
  }

  private def abbreviate(unit: TimeUnit): String = unit match {
    case NANOSECONDS => "ns"
    case MICROSECONDS => "μs"
    case MILLISECONDS => "ms"
    case SECONDS => "s"
    case _ => throw new AssertionError()
  }

  /** Creates and starts a new Stopwatch.
   *
   * @return running Stopwatch
   */
  def start(): Stopwatch = {
    new Stopwatch(true)
  }
}

/** Simple Stopwatch
 *
 * @author chaostone
 * @since 3.0.0
 */
class Stopwatch(val ticker: Ticker = Ticker.systemTicker(), immediately: Boolean = false) {

  /** True if stopwatch is currently running. */
  var running: Boolean = _

  private var elapsed: Long = _

  private var startTick: Long = _

  if (immediately) {
    running = true
    startTick = ticker.read()
  }

  /** Creates Stopwatch with system ticker; starts immediately if true. */
  def this(immediately: Boolean) = {
    this(Ticker.systemTicker(), immediately)
  }

  /** Starts the stopwatch.
   *
   * @return this `Stopwatch` instance
   */
  def start(): Stopwatch = {
    !running
    running = true
    startTick = ticker.read()
    this
  }

  /** Stops the stopwatch. Future reads will return the fixed duration that had
   * elapsed up to this point.
   *
   * @return this `Stopwatch` instance
   */
  def stop(): Stopwatch = {
    val tick = ticker.read()
    running = false
    elapsed += tick - startTick
    this
  }

  /** Sets the elapsed time for this stopwatch to zero,
   * and places it in a stopped state.
   *
   * @return this `Stopwatch` instance
   */
  def reset(): Stopwatch = {
    elapsed = 0
    running = false
    this
  }

  private def elapsedNanos: Long = if (running) ticker.read() - startTick + elapsed else elapsed

  /** Returns the current elapsed time in the desired unit (fraction rounded down).
   *
   * @param desiredUnit the time unit for the result
   * @return elapsed time in desiredUnit
   */
  def elapsedTime(desiredUnit: TimeUnit): Long = desiredUnit.convert(elapsedNanos, NANOSECONDS)

  /** Returns the current elapsed time shown on this stopwatch, expressed
   * in milliseconds, with any fraction rounded down. This is identical to
   * `elapsedTime(TimeUnit.MILLISECONDS`.
   */
  def elapsedMillis: Long = elapsedTime(MILLISECONDS)

  /** Returns a string representation of the current elapsed time
   * equivalent to `toString(4)` (four significant figures).
   */
  override def toString: String = format(elapsedNanos, 4)
}
