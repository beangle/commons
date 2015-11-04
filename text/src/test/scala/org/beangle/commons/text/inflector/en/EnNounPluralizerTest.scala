package org.beangle.commons.text.inflector.en

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers
import org.scalatest.FunSpec

/**
 * @author chaostone
 */
@RunWith(classOf[JUnitRunner])
class EnNounPluralizerTest extends FunSpec with Matchers {
  describe("EnNounPluralizer") {
    it("pluralize") {
      assert("accounts" == (new EnNounPluralizer).pluralize("account"))
    }
  }
}