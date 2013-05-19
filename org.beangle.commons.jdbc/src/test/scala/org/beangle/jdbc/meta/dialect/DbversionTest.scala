package org.beangle.jdbc.meta.dialect

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class DbversionTest extends FlatSpec with ShouldMatchers {

  "version [1.0,2.0] " should "contain both boundary" in {
    val version = new Dbversion("[1.0,2.0]");
    version.contains("1.0") should be(true)
    version.contains("1.0.1") should be(true)
    version.contains("1.5.2") should be(true)
    version.contains("2.0") should be(true)
    version.contains("0.9.beta") should be(false)
    version.contains("2.0.1") should be(false)
  }

  "single version" should "contain only one version" in {
    val version = new Dbversion("2.3.5");
    version.contains("2.3") should be(false)
    version.contains("2.4") should be(false)
    version.contains("2.3.5") should be(true)
  }

  "open range without end" should "contains version greate than start" in {
    val version = new Dbversion("(2.3.5,)");
    version.contains("2.3") should be(false)
    version.contains("2.4") should be(true)
    version.contains("2.5") should be(true)
  }

  "open range without start " should "contains version less than end" in {
    val version = new Dbversion("(,2.3.5]");
    version.contains("2.3.5") should be(true)
    version.contains("2.3") should be(true)
    version.contains("2.5") should be(false)
  }
}
