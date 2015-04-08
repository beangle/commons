/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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

import java.io.{ InputStream, InputStreamReader, LineNumberReader }
import java.nio.charset.Charset
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions.collectionAsScalaIterable

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ Charsets, ClassLoaders, Strings }
import org.beangle.commons.lang.annotation.description
/**
 * @since 3.0.0
 */
@description("缺省TextBundle注册表")
class DefaultTextBundleRegistry extends TextBundleRegistry {

  protected val caches = new collection.mutable.HashMap[Locale, ConcurrentHashMap[String, TextBundle]]

  protected val defaultBundleNames = new collection.mutable.ListBuffer[String]

  var reloadable: Boolean = false

  def addDefaults(bundleNames: String*) {
    defaultBundleNames ++= bundleNames
  }

  def load(locale: Locale, bundleName: String): TextBundle = {
    val localeBundles = caches.getOrElseUpdate(locale, new ConcurrentHashMap[String, TextBundle])
    var bundle = localeBundles.get(bundleName)
    if (null == bundle) {
      loadJavaBundle(bundleName, locale) match {
        case Some(b) => bundle = b
        case None =>
          loadNewBundle(bundleName, locale) foreach {
            case (name, nested) =>
              if (name == bundleName) bundle = nested
              else localeBundles.put(name, nested)
          }
      }
      localeBundles.put(bundleName, bundle)
    }
    if (reloadable) caches.clear
    bundle
  }

  protected def loadNewBundle(bundleName: String, locale: Locale): Map[String, TextBundle] = {
    val resource = toDefaultResourceName(bundleName, locale)
    val url = ClassLoaders.getResource(resource)
    if (null == url) {
      Map(bundleName -> new DefaultTextBundle(locale, resource, Map.empty))
    } else {
      val prefix = Strings.substringBeforeLast(bundleName, ".") + "."
      val bundles = readBundles(url.openStream).map {
        case (name, values) =>
          if (name.length == 0) (bundleName, new DefaultTextBundle(locale, resource, values))
          else (prefix + name, new DefaultTextBundle(locale, resource, values))
      }
      bundles.toMap
    }
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
  /**
   * Read key value properties
   * Group by Uppercased key,and default group
   */
  protected[i18n] def readBundles(input: InputStream, charset: Charset = Charsets.UTF_8): Map[String, Map[String, String]] = {
    if (null == input) Map.empty
    else {
      val defaults = ""
      val texts = new collection.mutable.HashMap[String, collection.mutable.HashMap[String, String]]
      val reader = new LineNumberReader(new InputStreamReader(input, charset))
      var line: String = reader.readLine
      while (null != line) {
        val index = line.indexOf('=')
        if (index > 0 && index != line.length - 1) {
          val key = line.substring(0, index).trim()
          val value = line.substring(index + 1).trim()
          if (Character.isUpperCase(key.charAt(0))) {
            val dotIdx = key.indexOf('.')
            if (-1 == dotIdx) {
              texts.getOrElseUpdate(defaults, new collection.mutable.HashMap[String, String]).put(key, value)
            } else {
              texts.getOrElseUpdate(key.substring(0, dotIdx), new collection.mutable.HashMap[String, String]).put(key.substring(dotIdx + 1), value)
            }
          } else {
            texts.getOrElseUpdate(defaults, new collection.mutable.HashMap[String, String]).put(key, value)
          }
        }
        line = reader.readLine()
      }
      if (!texts.contains(defaults)) texts.put(defaults, collection.mutable.HashMap.empty)
      val results = texts.map { case (name, values) => (name, values.toMap) }
      results.toMap
    }
  }
}
