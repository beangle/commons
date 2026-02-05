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

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{JVM, Strings}

object Enviroment {
  final val ProfileKey: String = "beangle.config.profiles"

  var Default: Enviroment = buildDefault()

  private def buildDefault(): Enviroment = {
    val env = new Enviroment
    env.addConfig(ConfigFactory.SystemEnvironment)
    env.addConfig(ConfigFactory.SystemProperties)
  }

  final def profiles: Set[String] = {
    val profiles = System.getProperty(ProfileKey)
    val profileSet = if (null == profiles) Set.empty else Strings.split(profiles, ",").toSet
    if JVM.isDebugMode then
      if profileSet.contains("-dev") then profileSet else profileSet + "dev"
    else
      profileSet
  }

  final def isDevMode: Boolean = profiles.contains("dev")

  final def isTestMode: Boolean = profiles.contains("test")
}

/** System Enviroment
 */
class Enviroment {
  private val configs = Collections.newBuffer[Config]

  private val cache = Collections.newMap[String, Any]

  private val processors = Collections.newBuffer[Config.Processor]

  private val resolving = Collections.newBuffer[String]

  def getProperty(name: String): Option[Any] = {
    getValue(name) match {
      case e@Some(v) => e
      case None =>
        val keys = configs.flatten(c => c.keys(name))
        val kvs = keys.map(k => (k, getValue(k).get)).toMap
        if (kvs.isEmpty) None else Some(kvs)
    }
  }

  /** 单纯的属性名称
   * 不是variable:defaultValue
   *
   * @param name property name
   * @return
   */
  def getValue(name: String): Option[Any] = {
    cache.get(name) match {
      case None =>
        var value: Any = null
        var config: Config = null
        configs.find { s =>
          value = s.getValue(name, null)
          config = s
          null != value
        }
        value match {
          case null => None
          case s: String =>
            val nvalue = process(name, s, config)
            cache.put(name, nvalue)
            Some(nvalue)
          case _ =>
            cache.put(name, value)
            Some(value)
        }
      case e@Some(v) => e
    }
  }

  def interpreter(holder: PlaceHolder): String = {
    val values = holder.variables map { v =>
      if (resolving.contains(v.name)) {
        val path = resolving.addOne(v.name).mkString("->")
        resolving.clear()
        throw new RuntimeException(s"Loop and recursive parsing :$path")
      }
      resolving.addOne(v.name)
      val pv = getProperty(v.name)
      resolving.subtractOne(v.name)
      val key = "${" + v.name + "}"
      (key, pv.orElse(v.defaultValue).getOrElse("${" + v.name + "}").toString)
    }
    var pattern = holder.pattern
    values foreach { case (k, v) =>
      pattern = Strings.replace(pattern, k, v)
    }
    pattern
  }

  private def process(key: String, value: String, config: Config): String = {
    var v = value
    if (!config.isResolved) {
      if (PlaceHolder.hasVariable(value)) {
        v = interpreter(PlaceHolder(value))
      }
    }
    processors.foreach(x => v = x.process(key, v))
    v
  }

  /** add fallback config
   *
   * @param cfg
   * @return
   */
  def addConfig(cfg: Config): Enviroment = {
    configs.addOne(cfg)
    this
  }

  def addProperties(properties: collection.Map[String, String]): Enviroment = {
    if (properties.nonEmpty) {
      configs.addOne(new SimpleConfig(properties))
    }
    this
  }

  def addProcessor(processor: Config.Processor): Enviroment = {
    this.processors.addOne(processor)
    this
  }

  def addProcessors(processors: Iterable[Config.Processor]): Enviroment = {
    this.processors.addAll(processors)
    this
  }
}
