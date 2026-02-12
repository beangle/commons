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

/** Configuration properties and processors. */
object Config {

  /** Provides a set of properties and optional processors. */
  trait Provider {
    /** Raw properties map. */
    def properties: collection.Map[String, String]

    /** Optional value processors (e.g. decrypt ENC(...)). */
    def processors: Seq[Config.Processor] = Seq.empty
  }

  /** Processes a property value (e.g. decrypt). */
  trait Processor {
    /** Processes the value for the given key; returns transformed value.
     *
     * @param key   the property name
     * @param value the raw value
     * @return processed value
     */
    def process(key: String, value: String): String
  }

  /** No-op processor; returns value unchanged. */
  object NoneProcessor extends Processor {
    override def process(key: String, value: String): String = value
  }

  /** Processor that decrypts ENC(...) wrapped property values.
   *
   * @param encryptor the PBE encryptor for decryption
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

    /** Wraps encrypted message as ENC(...) for config. */
    def encrypt(message: String): String = {
      require(null != message)
      head + encryptor.encrypt(message) + tail
    }
  }

  /** Returns a Processor that decrypts values using PBE with the given password. */
  def pbe(password: String): Processor = {
    require(Strings.isNotEmpty(password))
    PBEProcessor(PBEEncryptor.random(password))
  }

  /** Empty config; all lookups return default or throw. */
  object Empty extends Config {
    override def contains(name: String): Boolean = false

    override def get(name: String, defaults: Any): Any = defaults

    override def getValue(name: String, defaults: Any): Any = defaults

    override def keys(prefix: String): Iterable[String] = Seq.empty

    override def isResolved: Boolean = true
  }
}

/** Configuration interface for property lookup, nesting, and processors. */
trait Config {

  /** Returns true if the property exists. */
  def contains(name: String): Boolean

  /** Gets value, value seq, or map by path.
   *
   * @param name     path name
   * @param defaults default value when not found
   * @return the value
   */
  def get(name: String, defaults: Any): Any

  /** Gets raw value by path (no nesting).
   *
   * @param name     property name
   * @param defaults default when not found
   * @return the value
   */
  def getValue(name: String, defaults: Any): Any

  /** Returns keys with given prefix. */
  def keys(prefix: String): Iterable[String]

  /** True if placeholders have been resolved. */
  def isResolved: Boolean = false

  /** Gets value by name; throws RuntimeException if missing. */
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

  /** Wraps Java collections (List, Map) to Scala equivalents.
   *
   * @param value any value
   * @return Scala collection or original value
   */
  protected[config] final def wrap(value: Any): Any = {
    value match {
      case l: ju.List[_] => asScala(l)
      case m: ju.Map[_, _] => asScala(m)
      case _ => value
    }
  }

  /** Gets value as String. */
  final def getString(name: String, defaultValue: String = ""): String = {
    val value = getValue(name, null)
    if (null == value) defaultValue else value.toString
  }

  /** Gets value as Int. */
  final def getInt(name: String, defaultValue: Int = 0): Int = {
    val value = getValue(name, null)
    if (null == value) defaultValue else Numbers.toInt(name)
  }

  /** Gets value as Long. */
  final def getLong(name: String, defaultValue: Long = 0): Long = {
    val value = getValue(name, null)
    if (null == value) defaultValue else Numbers.toLong(name)
  }

  /** Gets value as Double. */
  final def getDouble(name: String, defaultValue: Double = 0): Double = {
    val value = getValue(name, null)
    if (null == value) defaultValue else Numbers.toDouble(name)
  }

  /** Gets value as Duration. */
  final def getDuration(name: String, defaultValue: Duration = Duration.ZERO): Duration = {
    val value = getValue(name, null)
    if (null == value) defaultValue else DurationConverter(name)
  }
}
