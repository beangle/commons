package org.beangle.commons.file.digest

import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import java.io.File

@RunWith(classOf[JUnitRunner])
class DigestTest extends FunSpec with Matchers {

  val file = new File("/tmp/pgadmin4-2.0.dmg")
  if (file.exists()) {
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
}