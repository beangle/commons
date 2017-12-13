package org.beangle.commons.file.digest

import java.io.{ File, FileInputStream }
import java.security.MessageDigest
import org.beangle.commons.codec.binary.Hex
import org.beangle.commons.io.IOs

abstract class AbstractFileDigest {

  final def digest(file: File): String = {
    if (!file.exists()) return "";

    val md = getAlgorithm()
    val buffer = new Array[Byte](4 * 1024)
    var count = 0
    val input = new FileInputStream(file)
    var n = input.read(buffer)
    val eof = IOs.eof

    while (eof != n) {
      md.update(buffer, 0, n)
      count += n
      n = input.read(buffer)
    }
    IOs.close(input)
    Hex.encode(md.digest())
  }
  
  protected def  getAlgorithm():MessageDigest
}