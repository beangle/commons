/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.text.seq
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HanZiSeqStyleTest extends AnyFunSpec with Matchers {

  describe("HanZiSeqStyle") {
    it("Build HanZi Sequence") {
      val style = new HanZiSeqStyle
      style.build(211) should equal("二百一十一")
      style.build(201) should equal("二百零一")
      style.build(3011) should equal("三千零十一")
    }
  }
}
