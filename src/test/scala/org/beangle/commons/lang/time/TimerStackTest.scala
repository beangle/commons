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

class TimerStackTest extends AnyFunSpec, Matchers {

  describe("TimerStack") {
    it("push pop peek") {
      val stack = new TimerStack()
      val root = new TimerNode("root", 0L)
      stack.push(root)
      stack.peek().resource shouldBe "root"

      val inner = new TimerNode("inner", 0L)
      stack.push(inner)
      stack.peek().resource shouldBe "inner"

      stack.pop().resource shouldBe "inner"
      stack.peek().resource shouldBe "root"

      stack.pop().resource shouldBe "root"
      stack.pop() shouldBe null
    }

    it("completeRoot") {
      val stack = new TimerStack()
      stack.minMs = 50
      val root = new TimerNode("root", 0L)
      root.totalTime = 10
      stack.completeRoot(root, 10)
      stack.completedRoots shouldBe empty

      root.totalTime = 60
      stack.completeRoot(root, 60)
      stack.completedRoots should contain theSameElementsAs Seq(root)
    }
  }
}
