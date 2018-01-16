package org.beangle.commons.file.digest

import java.security.MessageDigest

object MD5 extends AbstractFileDigest {

  protected override def getAlgorithm(): MessageDigest = {
    MessageDigest.getInstance("MD5")
  }
}