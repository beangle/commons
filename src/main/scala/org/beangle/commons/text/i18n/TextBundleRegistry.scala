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

/** Registry for loading and caching text bundles.
 *
 * @author chaostone
 * @since 3.0.0
 */
trait TextBundleRegistry {

  /** Loads a bundle for locale and name (cached). */
  def load(locale: Locale, bundleName: String): TextBundle

  /** Returns all loaded bundles for locale. */
  def getBundles(locale: Locale): List[TextBundle]

  /** Adds bundle names to the default lookup order. */
  def addDefaults(bundleNames: String*): Unit

  /** Looks up key in default bundles; returns first match or None. */
  def getDefaultText(key: String, locale: Locale): Option[String]
}
