/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.text.i18n.impl

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions.collectionAsScalaIterable

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ Arrays, ClassLoaders }
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.beangle.commons.text.i18n.spi.{ TextBundle, TextBundleRegistry }
/**
 * @since 3.0.0
 */
class DefaultTextBundleRegistry extends TextBundleRegistry with Logging {

  protected val caches = new collection.mutable.HashMap[Locale, ConcurrentHashMap[String, TextBundle]]

  protected val defaultBundleNames = new collection.mutable.ListBuffer[String]

  protected var reloadBundles: Boolean = false

  def addDefaults(bundleNames: String*) {
    defaultBundleNames ++= bundleNames
    info("Add " + Arrays.toString(bundleNames: _*) + " global message bundles")
  }

  def load(locale: Locale, bundleName: String): TextBundle = {
    if (reloadBundles) caches.clear()
    var localeBundles = caches.get(locale)
    if (localeBundles.isEmpty) {
      caches.synchronized {
        localeBundles = caches.get(locale)
        val newBundles = new ConcurrentHashMap[String, TextBundle]
        caches.put(locale, newBundles)
        localeBundles = Some(newBundles)
      }
    }
    var bundle = localeBundles.get.get(bundleName)
    if (null == bundle) {
      bundle = loadJavaBundle(bundleName, locale).getOrElse(
        loadNewBundle(bundleName, locale).getOrElse(new DefaultTextBundle(locale, bundleName, Map.empty[String, String])))
      localeBundles.get.put(bundleName, bundle)
    }
    bundle
  }

  protected def loadNewBundle(bundleName: String, locale: Locale): Option[TextBundle] = {
    val resource = toDefaultResourceName(bundleName, locale)
    val properties = IOs.readProperties(ClassLoaders.getResource(resource, getClass))
    Some(new DefaultTextBundle(locale, resource, properties))
  }

  /**
   * Load java properties bundle with iso-8859-1
   *
   * @param bundleName
   * @param locale
   * @return None or bundle corresponding bindleName.locale.properties
   */
  protected def loadJavaBundle(bundleName: String, locale: Locale): Option[TextBundle] = {
    val resource = toJavaResourceName(bundleName, locale)
    val properties = IOs.readJavaProperties(ClassLoaders.getResource(resource, getClass))
    if (properties.isEmpty) None else Some(new DefaultTextBundle(locale, resource, properties))
  }

  /**
   * java properties bundle name
   *
   * @param bundleName
   * @param locale
   * @return convented properties ended file path.
   */
  protected def toJavaResourceName(bundleName: String, locale: Locale): String = {
    var fullName = bundleName
    val localeName = toLocaleStr(locale)
    val suffix = "properties"
    if ("" != localeName) fullName = fullName + "_" + localeName
    val sb = new StringBuilder(fullName.length + 1 + suffix.length)
    sb.append(fullName.replace('.', '/')).append('.').append(suffix)
    sb.toString
  }

  /**
   * Generater resource name like bundleName.zh_CN
   *
   * @param bundleName
   * @param locale
   * @return resource name end with locale
   */
  protected def toDefaultResourceName(bundleName: String, locale: Locale): String = {
    val fullName = bundleName
    val localeName = toLocaleStr(locale)
    val suffix = localeName
    val sb = new StringBuilder(fullName.length + 1 + suffix.length)
    sb.append(fullName.replace('.', '/'))
    if ("" != suffix) sb.append('.').append(suffix)
    sb.toString
  }

  /**
   * Convert locale to string with language_country[_variant]
   *
   * @param locale
   * @return locale string
   */
  protected def toLocaleStr(locale: Locale): String = {
    if (locale == Locale.ROOT) {
      return ""
    }
    val language = locale.getLanguage
    val country = locale.getCountry
    val variant = locale.getVariant
    if (language == "" && country == "" && variant == "") {
      return ""
    }
    val sb = new StringBuilder()
    if (variant != "") {
      sb.append(language).append('_').append(country).append('_')
        .append(variant)
    } else if (country != "") {
      sb.append(language).append('_').append(country)
    } else {
      sb.append(language)
    }
    sb.toString
  }

  import collection.JavaConversions._
  def getBundles(locale: Locale): List[TextBundle] = {
    caches.get(locale) match {
      case Some(map) => map.values.toList
      case None => List.empty
    }
  }

  //FIXME
  def getDefaultText(key: String, locale: Locale): String = {
    var msg: String = null
    for (defaultBundleName <- defaultBundleNames) {
      load(locale, defaultBundleName).getText(key).foreach(x => return x)
    }
    null
  }

  def setReloadBundles(reloadBundles: Boolean) {
    this.reloadBundles = reloadBundles
  }
}
