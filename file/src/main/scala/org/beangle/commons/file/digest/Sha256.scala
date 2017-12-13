package org.beangle.commons.file.digest

import java.security.MessageDigest

object Sha256 extends AbstractFileDigest {

  protected override def getAlgorithm(): MessageDigest = {
    MessageDigest.getInstance("SHA-256")
  }
}