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

/** Environment config (profiles, property lookup). */
object Enviroment {

  /** System property key for active profiles (comma-separated). */
  final val ProfileKey: String = "beangle.config.profiles"

  /** Default Enviroment instance (system env + properties). */
  var Default: Enviroment = buildDefault()

  private def buildDefault(): Enviroment = {
    val env = new Enviroment
    env.addConfig(ConfigFactory.SystemEnvironment)
    env.addConfig(ConfigFactory.SystemProperties)
  }

  /** Active profiles; includes "dev" in debug mode if not specified. */
  final def profiles: Set[String] = {
    val profiles = System.getProperty(ProfileKey)
    val profileSet = if (null == profiles) Set.empty else Strings.split(profiles, ",").toSet
    if JVM.isDebugMode then
      if profileSet.contains("-dev") then profileSet else profileSet + "dev"
    else
      profileSet
  }

  /** Returns true if "dev" profile is active. */
  final def isDevMode: Boolean = profiles.contains("dev")

  /** Returns true if "test" profile is active. */
  final def isTestMode: Boolean = profiles.contains("test")
}

/** Environment config container (profiles, property lookup, cache). */
class Enviroment {
  private val configs = Collections.newBuffer[Config]

  private val cache = Collections.newMap[String, Any]

  private val processors = Collections.newBuffer[Config.Processor]

  private val resolving = Collections.newBuffer[String]

  /** Gets property value by name; supports nested keys and returns a map when multiple keys match.
   *
   * @param name the property name (may contain wildcards for nested lookup)
   * @return the value, a map of nested values, or None
   */
  def getProperty(name: String): Option[Any] = {
    getValue(name) match {
      case e@Some(v) => e
      case None =>
        val keys = configs.flatten(c => c.keys(name))
        val kvs = keys.map(k => (k, getValue(k).get)).toMap
        if (kvs.isEmpty) None else Some(kvs)
    }
  }

  /** Gets nested properties under the given path.
   *
   * @param path the path to properties (e.g. "a.b.c")
   * @return map of nested property names to values
   */
  def getNestedProperties(path: String): Map[String, String] = {
    getProperty(path) match {
      case None => Map.empty
      case Some(m) =>
        val prefixLength = (if (path.endsWith(".")) path else path + ".").length
        val values = m.asInstanceOf[collection.Map[String, _]]
        // Prevent underlying layer from doing type conversion
        values.map(x => (x._1.substring(prefixLength), getValue(x._1).get.toString)).toMap
    }
  }

  /** Gets the raw property value by name (not variable:defaultValue format).
   *
   * @param name the property name
   * @return the property value if found
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

  /** Resolves placeholders (e.g. ${var}) in the pattern using config values.
   *
   * @param holder the placeholder pattern holder
   * @return resolved string with placeholders substituted
   */
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

  /** Adds a config as fallback (later configs override earlier ones).
   *
   * @param cfg the config to add
   * @return this for chaining
   */
  def addConfig(cfg: Config): Enviroment = {
    configs.addOne(cfg)
    this
  }

  /** Adds properties as a new config layer.
   *
   * @param properties the properties to add
   * @return this for chaining
   */
  def addProperties(properties: collection.Map[String, String]): Enviroment = {
    if (properties.nonEmpty) {
      configs.addOne(new SimpleConfig(properties))
    }
    this
  }

  /** Adds a value processor (e.g. for variable substitution).
   *
   * @param processor the processor to add
   * @return this for chaining
   */
  def addProcessor(processor: Config.Processor): Enviroment = {
    this.processors.addOne(processor)
    this
  }

  /** Adds multiple value processors.
   *
   * @param processors the processors to add
   * @return this for chaining
   */
  def addProcessors(processors: Iterable[Config.Processor]): Enviroment = {
    this.processors.addAll(processors)
    this
  }
}
