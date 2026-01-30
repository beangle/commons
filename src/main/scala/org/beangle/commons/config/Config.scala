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

package org.beangle.commons.config

import org.beangle.commons.codec.binary.PBEEncryptor
import org.beangle.commons.conversion.string.DurationConverter
import org.beangle.commons.lang.{Numbers, Strings}

import java.time.Duration
import java.util as ju
import scala.jdk.javaapi.CollectionConverters.asScala

object Config {

  /** 提供一组Property属性和处理器
   */
  trait Provider {
    def properties: collection.Map[String, String]

    def processors: Seq[Config.Processor] = Seq.empty
  }

  trait Processor {
    def process(key: String, value: String): String
  }

  object NoneProcessor extends Processor {
    override def process(key: String, value: String): String = value
  }

  /** 可以对属性值进行解密的Processor
   *
   * @param encryptor pbe解密类
   */
  class PBEProcessor(encryptor: PBEEncryptor) extends Processor {
    private val head = "ENC("
    private val tail = ")"

    override def process(key: String, value: String): String = {
      if (null == value) {
        null
      } else {
        if (value.startsWith(head) && value.endsWith(tail)) {
          val msg = Strings.substringBetween(value, head, tail)
          encryptor.decrypt(msg)
        } else {
          value
        }
      }
    }

    def encrypt(message: String): String = {
      require(null != message)
      head + encryptor.encrypt(message) + tail
    }
  }

  /** 可以对属性值进行解密的Processor
   */
  def pbe(password: String): Processor = {
    require(Strings.isNotEmpty(password))
    PBEProcessor(PBEEncryptor.random(password))
  }

  object Empty extends Config {
    override def contains(name: String): Boolean = false

    override def get(name: String, defaults: Any): Any = null

    override def getValue(name: String, defaults: Any): Any = null

    override def keys(prefix: String): Iterable[String] = Seq.empty

    override def isResolved: Boolean = true
  }
}

trait Config {

  def contains(name: String): Boolean

  /** Get a value / value seq /  map
   *
   * @param name     path name
   * @param defaults default value
   * @return
   */
  def get(name: String, defaults: Any): Any

  def getValue(name: String, defaults: Any): Any

  def keys(prefix: String): Iterable[String]

  def isResolved: Boolean = false

  final def get(name: String): Any = {
    val v = get(name, null)
    if (null == v) {
      throw RuntimeException(s"Missing config property ${name}")
    } else {
      v
    }
  }

  protected[config] final def prefixOf(path: String): String = {
    if (path.endsWith(".")) path else path + "."
  }

  protected[config] final def convert(value: Any): Any = {
    value match {
      case l: ju.List[_] => asScala(l)
      case m: ju.Map[_, _] => asScala(m)
      case _ => value
    }
  }

  final def getInt(name: String, defaultValue: Int = 0): Int = {
    val value = get(name, null)
    if (null == value) defaultValue else Numbers.toInt(name)
  }

  final def getLong(name: String, defaultValue: Long = 0): Long = {
    val value = get(name, null)
    if (null == value) defaultValue else Numbers.toLong(name)
  }

  final def getDouble(name: String, defaultValue: Double = 0): Double = {
    val value = get(name, null)
    if (null == value) defaultValue else Numbers.toDouble(name)
  }

  //FIXME test duration
  final def getDuration(name: String, defaultValue: Duration = Duration.ZERO): Duration = {
    val value = get(name, null)
    if (null == value) defaultValue else DurationConverter(name)
  }
}
