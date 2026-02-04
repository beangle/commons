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

package org.beangle.commons.codec.binary

import org.beangle.commons.concurrent.Locks
import org.beangle.commons.lang.Charsets

import java.security.SecureRandom
import java.text.Normalizer
import java.util.concurrent.locks.ReentrantLock
import javax.crypto.spec.{IvParameterSpec, PBEKeySpec, PBEParameterSpec}
import javax.crypto.{Cipher, SecretKey, SecretKeyFactory}

/** 基于密码的加密工具类(Password-Based Encryption)
 */
object PBEEncryptor {

  /** The default salt size */
  val DefaultSaltSize = 8

  /** The default IV size */
  val DefaultIVSize = 16

  /** Generate key iterations */
  val DefaultIterations = 1000

  /** The default PBE algo */
  val DefaultAlgorithm = "PBEWithMD5AndDES"

  /** 根据密码和固定的盐以及初始向量生成加密和解密设施
   *
   * @param algorithm algo
   * @param password  plain
   * @param salt      fix salt
   * @param iv        fixed iv
   * @return
   */
  def fixed(algorithm: String, password: String, salt: Array[Byte], iv: Array[Byte]): FixedPBEEncryptor = {
    val encryptor = Cipher.getInstance(algorithm)
    val decryptor = Cipher.getInstance(algorithm)
    val blockSize = encryptor.getBlockSize
    val saltSize = getSaltSize(encryptor)
    val ivSize = getIVSize(encryptor)

    require(salt.length >= saltSize, s"salt contains ${salt.length} bytes,less than $saltSize.")
    require(iv.length >= ivSize, s"iv contains ${iv.length} bytes,less than $ivSize.")

    val saltBytes = if salt.length == saltSize then salt else left(salt, saltSize)
    val ivBytes = if iv.length == ivSize then iv else left(iv, ivSize)
    val key = buildKey(algorithm, password)
    val parameter = buildParameter(saltBytes, ivBytes, DefaultIterations)
    init(key, parameter, encryptor, decryptor)
    FixedPBEEncryptor(encryptor, decryptor)
  }

  def random(password: String): RandomPBEEncryptor = {
    random(DefaultAlgorithm, password)
  }

  /** 根据随机盐和随机iv生成的加密/解密设施
   *
   * @param algorithm algo
   * @param password  plain text
   * @return
   */
  def random(algorithm: String, password: String): RandomPBEEncryptor = {
    val encryptor = Cipher.getInstance(algorithm)
    val decryptor = Cipher.getInstance(algorithm)
    val saltSize = getSaltSize(encryptor)
    val ivSize = getIVSize(encryptor)
    RandomPBEEncryptor(encryptor, decryptor, buildKey(algorithm, password), saltSize, ivSize)
  }

  def generateSaltAndIv(algorithm: String): (String, String) = {
    val encryptor = Cipher.getInstance(algorithm)
    val salt = Array.ofDim[Char](getSaltSize(encryptor))
    val iv = Array.ofDim[Char](getIVSize(encryptor))
    salt.indices foreach { i =>
      salt(i) = scala.util.Random.nextPrintableChar()
    }
    iv.indices foreach { i =>
      iv(i) = scala.util.Random.nextPrintableChar()
    }
    (new String(salt), new String(iv))
  }

  /** 将一个普通的密码转成PBE所需的密钥格式
   * 不保留密码铭文本身
   *
   * @param algorithm pbe algo
   * @param password  plain password
   * @return
   */
  def buildKey(algorithm: String, password: String): SecretKey = {
    SecretKeyFactory.getInstance(algorithm).generateSecret(new PBEKeySpec(normalize(password)))
  }

  /** 根据盐和初始向量构建加密参数
   *
   * @param salt salt bytes
   * @param iv   initialization vector bytes
   */
  def buildParameter(salt: Array[Byte], iv: Array[Byte], iterations: Int): PBEParameterSpec = {
    new PBEParameterSpec(salt, iterations, new IvParameterSpec(iv))
  }

  /** 提供固定盐和初始向量的加密器
   *
   * @param encryptor encryptor
   * @param decryptor decryptor
   */
  class FixedPBEEncryptor(encryptor: Cipher, decryptor: Cipher) extends PBEEncryptor {

    private val lock = new ReentrantLock

    override def encrypt(message: String): String = {
      val encrypted = Locks.withLock(lock)(encryptor.doFinal(message.getBytes(Charsets.UTF_8)))
      new String(java.util.Base64.getEncoder.encode(encrypted))
    }

    override def decrypt(message: String): String = {
      val encrypted = java.util.Base64.getDecoder.decode(message)
      Locks.withLock(lock)(new String(decryptor.doFinal(encrypted)))
    }
  }

  /** 随机生成盐和iv的加密器
   *
   * @param encryptor encryptor
   * @param decryptor decryptor
   * @param key       key
   * @param saltSize  salt byte size
   * @param ivSize    iv byte size
   */
  class RandomPBEEncryptor(encryptor: Cipher, decryptor: Cipher, key: SecretKey, saltSize: Int, ivSize: Int) extends PBEEncryptor {

    var iterations: Int = 1000
    private val lock = new ReentrantLock
    private val random = SecureRandom.getInstance("SHA1PRNG")

    /** 对输入进行加密，返回格式为salt+iv+encrypted
     *
     * @param message input string
     * @return
     */
    override def encrypt(message: String): String = {
      val salt = randomBytes(saltSize)
      val iv = randomBytes(ivSize)
      val parameter = buildParameter(salt, iv, iterations)
      var encrypted: Array[Byte] = null
      Locks.withLock(lock) {
        this.encryptor.init(Cipher.ENCRYPT_MODE, key, parameter)
        encrypted = this.encryptor.doFinal(message.getBytes(Charsets.UTF_8))
      }
      val prefix = concat(salt, iv)
      new String(java.util.Base64.getEncoder.encode(concat(prefix, encrypted)))
    }

    override def decrypt(message: String): String = {
      val encrypted = java.util.Base64.getDecoder.decode(message)
      val msgSize = encrypted.length - saltSize - ivSize
      require(msgSize > 0, "message is too short")

      val salt = Array.ofDim[Byte](saltSize)
      val iv = Array.ofDim[Byte](ivSize)
      val msg = Array.ofDim[Byte](msgSize)
      System.arraycopy(encrypted, 0, salt, 0, saltSize)
      System.arraycopy(encrypted, saltSize, iv, 0, ivSize)
      System.arraycopy(encrypted, saltSize + ivSize, msg, 0, msgSize)
      val parameter = buildParameter(salt, iv, iterations)

      var decrypted: Array[Byte] = null
      Locks.withLock(lock) {
        this.decryptor.init(Cipher.DECRYPT_MODE, key, parameter)
        decrypted = this.decryptor.doFinal(msg)
      }
      new String(decrypted, Charsets.UTF_8)
    }

    private def randomBytes(len: Int): Array[Byte] = {
      val buf = Array.ofDim[Byte](len)
      Locks.withLock(lock) {
        this.random.nextBytes(buf)
      }
      buf
    }

    private def concat(first: Array[Byte], second: Array[Byte]): Array[Byte] = {
      val result = Array.ofDim[Byte](first.length + second.length)
      System.arraycopy(first, 0, result, 0, first.length)
      System.arraycopy(second, 0, result, first.length, second.length)
      result
    }
  }

  /** 初始化加密和解密模式
   *
   * @param key       key
   * @param parameter parameter
   * @param encryptor encryptor
   * @param decryptor decryptor
   */
  private def init(key: SecretKey, parameter: PBEParameterSpec, encryptor: Cipher, decryptor: Cipher): Unit = {
    encryptor.init(Cipher.ENCRYPT_MODE, key, parameter)
    decryptor.init(Cipher.DECRYPT_MODE, key, parameter)
  }

  private def normalize(password: String): Array[Char] = {
    Normalizer.normalize(password, Normalizer.Form.NFC).toCharArray
  }

  private def getSaltSize(encryptor: Cipher): Int = {
    val blockSize = encryptor.getBlockSize
    if blockSize > 0 then blockSize else DefaultSaltSize
  }

  private def getIVSize(encryptor: Cipher): Int = {
    val blockSize = encryptor.getBlockSize
    if blockSize > 0 then blockSize else DefaultIVSize
  }

  private def left(data: Array[Byte], len: Int): Array[Byte] = {
    val rs = Array.ofDim[Byte](len)
    System.arraycopy(data, 0, rs, 0, len)
    rs
  }
}

trait PBEEncryptor {
  def encrypt(message: String): String

  def decrypt(message: String): String
}
