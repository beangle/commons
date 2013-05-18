/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.text.i18n

import java.util.Locale

/**
 * TextBundleRegistry
 *
 * @author chaostone
 * @since 3.0.0
 */
trait TextBundleRegistry {

  /**
   * Load and cache bundle
   *
   * @param locale
   * @param bundleName
   */
  def load(locale: Locale, bundleName: String): TextBundle

  /**
   * List locale bundles
   *
   * @return empty list when not found
   */
  def getBundles(locale: Locale): List[TextBundle]

  /**
   * Load and cache default bundles
   *
   * @param bundleNames
   */
  def addDefaults(bundleNames: String*): Unit

  /**
   * Get default locale message
   *
   * @param key
   * @param locale
   * @return null when not found
   */
  def getDefaultText(key: String, locale: Locale): String

  /**
   * Whether cache bundles
   *
   * @param reloadBundles
   */
  def setReloadBundles(reloadBundles: Boolean): Unit
}
