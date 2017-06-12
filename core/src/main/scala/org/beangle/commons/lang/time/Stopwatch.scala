/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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

import java.util.concurrent.TimeUnit.MICROSECONDS
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeUnit
import org.beangle.commons.lang.Assert

object Stopwatch {

  /**
   * Returns a string representation of the current elapsed time, choosing an
   * appropriate unit and using the specified number of significant figures.
   * For example, at the instant when {@code elapsedTime(NANOSECONDS)} would
   * return {1234567}, {@code toString(4)} returns {@code "1.235 ms"}.
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
}
import Stopwatch._
/**
 * Simple Stopwatch
 * @author chaostone
 * @since 3.0.0
 */
class Stopwatch(val ticker: Ticker = Ticker.systemTicker(), start: Boolean = false) {

  var running: Boolean = _

  private var elapsed: Long = _

  private var startTick: Long = _

  if (start) {
    running = true
    startTick = ticker.read()
  }

  def this(start: Boolean) {
    this(Ticker.systemTicker(), start)
  }
  /**
   * Starts the stopwatch.
   *
   * @return this {@code Stopwatch} instance
   * @throws IllegalStateException if the stopwatch is already running.
   */
  def start(): Stopwatch = {
    !running
    running = true
    startTick = ticker.read()
    this
  }

  /**
   * Stops the stopwatch. Future reads will return the fixed duration that had
   * elapsed up to this point.
   *
   * @return this {@code Stopwatch} instance
   * @throws IllegalStateException if the stopwatch is already stopped.
   */
  def stop(): Stopwatch = {
    val tick = ticker.read()
    running = false
    elapsed += tick - startTick
    this
  }

  /**
   * Sets the elapsed time for this stopwatch to zero,
   * and places it in a stopped state.
   *
   * @return this {@code Stopwatch} instance
   */
  def reset(): Stopwatch = {
    elapsed = 0
    running = false
    this
  }

  private def elapsedNanos: Long = if (running) ticker.read() - startTick + elapsed else elapsed

  /**
   * Returns the current elapsed time shown on this stopwatch, expressed
   * in the desired time unit, with any fraction rounded down.
   * <p>
   * Note that the overhead of measurement can be more than a microsecond, so it is generally not
   * useful to specify {@link TimeUnit#NANOSECONDS} precision here.
   */
  def elapsedTime(desiredUnit: TimeUnit): Long = desiredUnit.convert(elapsedNanos, NANOSECONDS)

  /**
   * Returns the current elapsed time shown on this stopwatch, expressed
   * in milliseconds, with any fraction rounded down. This is identical to
   * {@code elapsedTime(TimeUnit.MILLISECONDS}.
   */
  def elapsedMillis: Long = elapsedTime(MILLISECONDS)

  /**
   * Returns a string representation of the current elapsed time; equivalent to {@code toString(4)}
   * (four significant figures).
   */
  override def toString(): String = format(elapsedNanos, 4)
}
