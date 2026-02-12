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

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.{Charsets, ClassLoaders, Strings}

import java.io.{InputStream, InputStreamReader, LineNumberReader}
import java.nio.charset.Charset
import java.util
import java.util.Locale

/** Default TextBundle registry with caching.
 *
 * @since 3.0.0
 */
@description("Default TextBundle registry")
class DefaultTextBundleRegistry extends TextBundleRegistry {

  /** Loader for fetching bundles from classpath/files. */
  var loader: TextBundleLoader = new DefaultTextBundleLoader
  protected var caches: Map[Locale, Map[String, TextBundle]] = Map.empty

  protected val defaultBundleNames = new collection.mutable.ListBuffer[String]

  override def addDefaults(bundleNames: String*): Unit = {
    defaultBundleNames ++= bundleNames
  }

  override def load(locale: Locale, bundleName: String): TextBundle = {
    caches.get(locale) match
      case None =>
        val results = loader.load(locale, bundleName)
        caches += (locale, results)
        results(bundleName)
      case Some(loaded) =>
        loaded.get(bundleName) match
          case None =>
            val results = loader.load(locale, bundleName)
            caches += (locale, loaded ++ results)
            results(bundleName)
          case Some(bundle) => bundle
  }

  override def getBundles(locale: Locale): List[TextBundle] = {
    caches.get(locale) match {
      case Some(map) => map.values.toList
      case None => List.empty
    }
  }

  override def getDefaultText(key: String, locale: Locale): Option[String] = {
    var msg: Option[String] = None
    defaultBundleNames find { bundleName =>
      val bundle = load(locale, bundleName)
      msg = bundle.get(key)
      msg.isDefined
    }
    msg
  }

}
