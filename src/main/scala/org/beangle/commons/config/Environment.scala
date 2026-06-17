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

import org.beangle.commons.lang.{JVM, Strings}

/** Environment config (profiles, property lookup). */
object Environment {

  /** System property key for active profiles (comma-separated). */
  final val ProfileKey: String = "beangle.config.profiles"

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

trait Environment {
  /** Gets property value by name; supports nested keys and returns a map when multiple keys match.
   *
   * @param name the property name (may contain wildcards for nested lookup)
   * @return the value, a map of nested values, or None
   */
  def getProperty(name: String): Option[Any]

  /** Gets the raw property value by name (not variable:defaultValue format).
   *
   * @param name the property name
   * @return the property value if found
   */
  def getValue(name: String): Option[Any]

  /** Gets nested properties under the given path.
   *
   * @param path the path to properties (e.g. "a.b.c")
   * @return map of nested property names to values
   */
  def getNested(path: String): Map[String, Any]

  /** Resolves placeholders (e.g. ${var}) in the pattern using config values.
   *
   * @param holder the placeholder pattern holder
   * @return resolved string with placeholders substituted
   */
  def interpreter(holder: PlaceHolder): String
}
