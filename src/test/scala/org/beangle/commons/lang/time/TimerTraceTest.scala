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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TimerTraceTest extends AnyFunSpec, Matchers {

  private class CapturingReporter extends TimerReporter {
    val roots = scala.collection.mutable.ListBuffer.empty[TimerNode]
    override def report(root: TimerNode): Unit = roots += root
  }

  describe("TimerTrace") {
    it("nested trace reports tree when runScoped exits") {
      val capturing = new CapturingReporter
      TimerTrace.runScoped(reporter = capturing) {
        TimerTrace.trace("root") {
          TimerTrace.trace("inner") {
            Thread.sleep(2)
          }
        }
      }
      capturing.roots should have size 1
      capturing.roots.head.resource shouldBe "root"
      capturing.roots.head.children should have size 1
      capturing.roots.head.children.head.resource shouldBe "inner"
    }

    it("trace requires runScoped") {
      intercept[IllegalArgumentException] {
        TimerTrace.trace("x") { () }
      }
    }

    it("minMs filters fast child spans") {
      val capturing = new CapturingReporter
      TimerTrace.runScoped(reporter = capturing) {
        TimerTrace.trace("root", minMs = 50) {
          TimerTrace.trace("slow") { Thread.sleep(60) }
          TimerTrace.trace("fast") { () }
        }
      }
      capturing.roots should have size 1
      capturing.roots.head.children.map(_.resource) shouldBe List("slow")
    }

    it("reports after body completes") {
      val capturing = new CapturingReporter
      TimerTrace.runScoped(reporter = capturing) {
        TimerTrace.trace("root") { () }
        capturing.roots shouldBe empty
      }
      capturing.roots should have size 1
    }

    it("reporter output: 3 levels with 2 branches per level") {
      val capturing = new CapturingReporter
      val reporter: TimerReporter = root => {
        capturing.report(root)
        println("=== TimerTrace reporter output ===")
        println(root.getPrintable)
        println("==================================")
      }
      TimerTrace.runScoped(reporter = reporter) {
        TimerTrace.trace("L0-request") {
          TimerTrace.trace("L1-service-A") {
            TimerTrace.trace("L2-dao-A1") { Thread.sleep(5) }
            TimerTrace.trace("L2-dao-A2") { Thread.sleep(8) }
          }
          TimerTrace.trace("L1-service-B") {
            TimerTrace.trace("L2-dao-B1") { Thread.sleep(3) }
            TimerTrace.trace("L2-dao-B2") { Thread.sleep(6) }
          }
        }
      }
      capturing.roots should have size 1
      val root = capturing.roots.head
      root.resource shouldBe "L0-request"
      root.children.map(_.resource) shouldBe List("L1-service-A", "L1-service-B")
      root.children.head.children.map(_.resource) shouldBe List("L2-dao-A1", "L2-dao-A2")
      root.children(1).children.map(_.resource) shouldBe List("L2-dao-B1", "L2-dao-B2")
    }
  }
}
