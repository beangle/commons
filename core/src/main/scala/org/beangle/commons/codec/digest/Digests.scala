package org.beangle.commons.codec.digest

import org.beangle.commons.codec.binary.Hex
import org.beangle.commons.codec.binary.HexEncoder
import java.security.MessageDigest
import org.beangle.commons.lang.Charsets._
/**
 * Digest tools
 */
object Digests {

  def md5: MessageDigest = MessageDigest.getInstance("MD5")

  def sha1: MessageDigest = MessageDigest.getInstance("SHA-1")

  def md5Hex(bytes: Array[Byte]): String = HexEncoder.encode(md5.digest(bytes))

  def md5Hex(string: String): String = HexEncoder.encode(md5.digest(string.getBytes(UTF_8)))

  def sha1Hex(bytes: Array[Byte]): String = HexEncoder.encode(sha1.digest(bytes))

  def sha1Hex(string: String): String = HexEncoder.encode(sha1.digest(string.getBytes(UTF_8)))

}