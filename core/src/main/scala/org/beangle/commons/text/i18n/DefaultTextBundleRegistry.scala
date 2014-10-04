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
package org.beangle.commons.text.i18n

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions.collectionAsScalaIterable

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ Arrays, ClassLoaders }
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.logging.Logging
/**
 * @since 3.0.0
 */
@description("缺省TextBundle注册表")
class DefaultTextBundleRegistry extends TextBundleRegistry with Logging {

  protected val caches = new collection.mutable.HashMap[Locale, ConcurrentHashMap[String, TextBundle]]

  protected val defaultBundleNames = new collection.mutable.ListBuffer[String]

  var reloadable: Boolean = false

  def addDefaults(bundleNames: String*) {
    defaultBundleNames ++= bundleNames
    info("Add " + Arrays.toString(bundleNames: _*) + " global message bundles")
  }

  def load(locale: Locale, bundleName: String): TextBundle = {
    if (reloadable) {
      loadJavaBundle(bundleName, locale).getOrElse(
        loadNewBundle(bundleName, locale).getOrElse(new DefaultTextBundle(locale, bundleName, Map.empty[String, String])))
    } else {
      val localeBundles = caches.get(locale).getOrElse {
        caches.synchronized {
          val newBundles = new ConcurrentHashMap[String, TextBundle]
          caches.put(locale, newBundles)
          newBundles
        }
      }
      var bundle = localeBundles.get(bundleName)
      if (null == bundle) {
        bundle = loadJavaBundle(bundleName, locale).getOrElse(
          loadNewBundle(bundleName, locale).getOrElse(new DefaultTextBundle(locale, bundleName, Map.empty[String, String])))
        localeBundles.put(bundleName, bundle)
      }
      bundle
    }
  }

  protected def loadNewBundle(bundleName: String, locale: Locale): Option[TextBundle] = {
    val resource = toDefaultResourceName(bundleName, locale)
    val properties = IOs.readProperties(ClassLoaders.getResource(resource))
    Some(new DefaultTextBundle(locale, resource, properties))
  }

  /**
   * Load java properties bundle with iso-8859-1
   */
  protected def loadJavaBundle(bundleName: String, locale: Locale): Option[TextBundle] = {
    val resource = toJavaResourceName(bundleName, locale)
    val properties = IOs.readJavaProperties(ClassLoaders.getResource(resource))
    if (properties.isEmpty) None else Some(new DefaultTextBundle(locale, resource, properties))
  }

  /**
   * java properties bundle name
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
   */
  protected def toLocaleStr(locale: Locale): String = {
    if (locale == Locale.ROOT) return ""
    val language = locale.getLanguage
    val country = locale.getCountry
    val variant = locale.getVariant
    if (language == "" && country == "" && variant == "") return ""
    val sb = new StringBuilder()
    if (variant != "") {
      sb.append(language).append('_').append(country).append('_').append(variant)
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

  def getDefaultText(key: String, locale: Locale): Option[String] = {
    var msg: Option[String] = None
    defaultBundleNames find { bundleName =>
      val bundle = load(locale, bundleName)
      msg = bundle.get(key)
      None != msg
    }
    msg
  }
}
