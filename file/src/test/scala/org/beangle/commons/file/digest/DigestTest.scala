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

package org.beangle.commons.file.digest

import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec

import java.io.File

class DigestTest extends AnyFunSpec with Matchers {

  val file = new File("/tmp/pgadmin4-2.0.dmg")
  if (file.exists())
    describe("Digest") {
      it("md5") {
        val digest = MD5.digest(file)
        "ceff1ea5976548d7e840e74838b53fa3" should equal(digest)
      }
      it("sha1") {
        val digest = Sha1.digest(file)
        "00a7e4c5509ffd2e9cc7126280683b4d9e118253" should equal(digest)
      }
      it("sha256") {
        val digest = Sha256.digest(file)
        "918d0fd4d9c743e44bdb7e5c9d96cb6c759c6c3aa49e5ddb6301c3ce49000c74" should equal(digest)
      }
    }
}
