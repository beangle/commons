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

package org.beangle.commons.os

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class WinCmdTest extends AnyFunSpec, Matchers {

  describe("WinCmd") {
    it("find command") {
      WinCmd.find("soffice.exe") foreach { p =>
        println(p)
        val rs = WinCmd.exec(p.toAbsolutePath.toString.replace(".exe", ".com"), "--version")
        rs._2 foreach println
      }
      WinCmd.exec("dir")._2 foreach println

      WinCmd.killall("soffice.bin")
    }
  }
}
