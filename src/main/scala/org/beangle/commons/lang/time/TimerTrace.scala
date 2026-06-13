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

import org.beangle.commons.lang.ScopedContext
import org.beangle.commons.logging.slf4j

/** Hierarchical performance profiler in `ScopedContext`.
 *
 * One `TimerStack` per `runScoped`: `trace` builds span trees on the stack; completed roots
 * are kept on the stack; `reporter` outputs them when the scope exits.
 *
 * To disable profiling, omit `runScoped` at the request boundary or do not call `trace`.
 *
 * Usage — servlet filter:
 * {{{
 * TimerTrace.runScoped(reporter = TimerTrace.logReporter) {
 *   chain.doFilter(request, response)
 * }
 * }}}
 *
 * Usage — nested spans:
 * {{{
 * TimerTrace.trace("loadUser", minMs = 50) {
 *   TimerTrace.trace("queryDb") {
 *     dao.find(id)
 *   }
 * }
 * }}}
 */
object TimerTrace {

  private val logger = slf4j("org.beangle.commons.lang.time.TimerTrace")

  private val stackKey = ScopedContext.Key[TimerStack]("beangle.commons.timer-stack")

  /** Default reporter: logs each tree at INFO via SLF4J. */
  val logReporter: TimerReporter = (root: TimerNode) => logger.info(root.getPrintable)

  /** Runs `body` in a scope; calls `reporter` for each completed root tree when the scope exits. */
  def runScoped[T](reporter: TimerReporter = logReporter)(body: => T): T = {
    ScopedContext.runWith(stackKey -> new TimerStack()) {
      try body
      finally ScopedContext.get(stackKey).foreach(s => s.completedRoots.foreach(reporter.report))
    }
  }

  /** Times `body` under `name`; must run inside `runScoped`.
   *
   * @param name  span name
   * @param minMs minimum duration in ms to keep child spans and the root tree (default 0);
   *              applied when a new root span starts
   */
  def trace[T](name: String, minMs: Int = 0)(body: => T): T = {
    require(ScopedContext.get(stackKey).isDefined, "TimerTrace.trace requires runScoped { ... }")
    beginSpan(name, minMs)
    try body finally finishSpan()
  }

  private def beginSpan(name: String, minMs: Int): Unit = {
    val stack = ScopedContext.get(stackKey).get
    if (stack.peek() == null) stack.minMs = minMs
    stack.push(new TimerNode(name, System.currentTimeMillis()))
  }

  private def finishSpan(): Unit = {
    ScopedContext.get(stackKey).foreach { stack =>
      val current = stack.pop()
      if (current != null) {
        val elapsed = current.end()
        if (stack.peek() == null) stack.completeRoot(current, elapsed)
        else if (elapsed > stack.minMs) stack.peek().children += current
      }
    }
  }
}
