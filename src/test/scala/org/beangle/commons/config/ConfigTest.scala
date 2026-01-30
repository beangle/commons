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

package org.beangle.commons.config

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ConfigTest extends AnyFunSpec, Matchers {
  val encryptKey = "beangle"
  val password = "bZoTxh)foA"

  describe("Config") {
    it("PBEDecode process") {
      System.setProperty("beangle.encryptor.password", encryptKey)
      val decoder = Config.pbe(encryptKey)
      val decryptedPwd = decoder.process("password", "ENC(Rr/iQsiRlgm/QWpa17YYVZmEPbU0RoZ6F0f4OU3u5DM=)")
      assert(password == decryptedPwd)
      assert("+some_test" == decoder.process("key", "+some_test"))
    }
  }

}
