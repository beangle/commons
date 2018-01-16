package org.beangle.commons.file.digest

import java.security.MessageDigest

object Sha1 extends AbstractFileDigest {

  protected override def getAlgorithm(): MessageDigest = {
    MessageDigest.getInstance("SHA1")
  }
}