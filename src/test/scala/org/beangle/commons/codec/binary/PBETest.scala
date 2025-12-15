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

package org.beangle.commons.codec.binary

import org.beangle.commons.cdi.{PBEPropertyResolver, PropertyResolver}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

class PBETest extends AnyFunSpec, Matchers {
  val encryptKey = "beangle"
  val password = "bZoTxh)foA"
  describe("PBE") {

    it("encrypt and decrypt") {
      val pbe = PBEEncryptor.random(encryptKey)
      assert(password == pbe.decrypt("Rr/iQsiRlgm/QWpa17YYVZmEPbU0RoZ6F0f4OU3u5DM="))
    }

    it("PBEDecode process") {
      System.setProperty("jasypt.encryptor.password", encryptKey)
      val decoder = PBEPropertyResolver.jasypt()
      val decryptedPwd = decoder.resolve("ENC(Rr/iQsiRlgm/QWpa17YYVZmEPbU0RoZ6F0f4OU3u5DM=)")
      assert(password == decryptedPwd)
      assert("+some_test" == decoder.resolve("+some_test"))
    }

    it("generate salt and iv") {
      val rs = PBEEncryptor.generateSaltAndIv(PBEEncryptor.DefaultAlgorithm)
      assert(rs._1 != null)
      assert(rs._2 != null)
    }
  }
}
