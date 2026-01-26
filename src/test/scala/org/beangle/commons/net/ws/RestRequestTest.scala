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

package org.beangle.commons.net.ws

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class RestRequestTest extends AnyFunSpec, Matchers {

  describe("RestRequest") {
    it("resolve url") {
      val request = RestRequest("http://localhost:8080", new RestClient)
      request.path("/depart/{departId}/user/{id}/detail", 12, 345L).params("showPhoto" -> true)
      request.buildURL().toString should equal("http://localhost:8080/depart/12/user/345/detail?showPhoto=true")

      val newUrl = request.path("/user/{id}/detail?enableCopy=1", 345L).params("banner" -> "某某信息系统").buildURL().toString
      newUrl should equal("http://localhost:8080/user/345/detail?enableCopy=1&showPhoto=true&banner=%E6%9F%90%E6%9F%90%E4%BF%A1%E6%81%AF%E7%B3%BB%E7%BB%9F")
    }
  }
}
