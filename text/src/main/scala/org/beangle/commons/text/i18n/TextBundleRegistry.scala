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
   */
  def load(locale: Locale, bundleName: String): TextBundle

  /**
   * List locale bundles
   */
  def getBundles(locale: Locale): List[TextBundle]

  /**
   * Load and cache default bundles
   */
  def addDefaults(bundleNames: String*): Unit

  /**
   * Get default locale message
   */
  def getDefaultText(key: String, locale: Locale): Option[String]

  def reloadable: Boolean

  def reloadable_=(value: Boolean): Unit
}
