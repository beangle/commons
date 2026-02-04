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

package org.beangle.commons.xml

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class DocumentTest extends AnyFunSpec, Matchers {
  val doc = Document.parse(IOs.readString(ClassLoaders.getResource("beangle.xml").get.openStream()))

  describe("Document") {
    it("parse") {
      val logbackxml =
        """
          |<configuration debug="false">
          |  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
          |    <encoder>
          |      <pattern>%d{MM-dd HH:mm:ss.SSS} %-5level %logger{30} - %msg%n</pattern>
          |    </encoder>
          |  </appender>
          |  <root level="INFO">
          |    <appender-ref ref="STDOUT"/>
          |  </root>
          |</configuration>
          |
          |""".stripMargin
      val config = Document.parse(logbackxml)
      (config \ "appender" \ "encoder" \ "pattern") foreach { e =>
        println(e.text)
      }
    }
    it("get attribute") {
      (doc \ "web" \ "initializer") foreach { i =>
        println((i \ "@class").text)
      }
    }
    it("get getDescendants") {
      val bundles = (doc \\ "static" \\ "bundle")
      assert(21 == bundles.size)
    }
    it("get children") {
      (doc \ "_").nonEmpty should be(true)
    }
  }

}
