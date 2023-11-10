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

/** @since 3.0.0
 */
@description("缺省TextBundle注册表")
class DefaultTextBundleRegistry extends TextBundleRegistry {

  protected var caches: Map[Locale, Map[String, TextBundle]] = Map.empty

  protected val defaultBundleNames = new collection.mutable.ListBuffer[String]

  var reloadable: Boolean = false

  def addDefaults(bundleNames: String*): Unit =
    defaultBundleNames ++= bundleNames

  def load(locale: Locale, bundleName: String): TextBundle = {
    if (reloadable) {
      loadBundles(locale, bundleName).apply(bundleName)
    } else {
      val results = loadBundles(locale, bundleName)
      caches.get(locale) match
        case None => caches += (locale, results)
        case Some(loaded) => caches += (locale, loaded ++ results)
      results.apply(bundleName)
    }
  }

  def loadBundles(locale: Locale, bundleName: String): Map[String, DefaultTextBundle] = {
    val results = Collections.newMap[String, DefaultTextBundle]
    val bundles = findBundles(locale, bundleName)
    if (bundles.isEmpty) {
      results.put(bundleName, new DefaultTextBundle(locale, bundleName, Map.empty))
    } else {
      val prefix = Strings.substringBeforeLast(bundleName, ".") + "."

      bundles foreach { b =>
        readBundle(b._2) foreach {
          case (name, values) =>
            val key = if name.isEmpty then bundleName else prefix + name
            results.get(key) match
              case None => results.put(key, new DefaultTextBundle(locale, b._1, values))
              case Some(existed) => results.put(key, existed.merge(new DefaultTextBundle(locale, b._1, values)))
        }
        if !results.contains(bundleName) then
          results.put(bundleName, new DefaultTextBundle(locale, bundleName, Map.empty))
      }
    }
    results.toMap
  }

  protected def findBundles(locale: Locale, bundleName: String): collection.Seq[(String, InputStream)] = {
    val inputs = Collections.newBuffer[(String, InputStream)]
    val fullName = bundleName.replace('.', '/')
    val localeName = toLocaleStr(locale)
    if ("" == localeName){
      //find java properties(utf-8)
      ClassLoaders.getResource(s"${fullName}.properties") foreach { b =>
        inputs.addOne((b.toString, b.openStream()))
      }
    }else{
      //find java properties(utf-8)
      ClassLoaders.getResource(s"${fullName}_${localeName}.properties") foreach { b =>
        inputs.addOne((b.toString, b.openStream()))
      }
      //Generate name like bundleName.zh_CN
      ClassLoaders.getResource(s"${fullName}.${localeName}") foreach { b =>
        inputs.addOne((b.toString, b.openStream()))
      }
    }
    inputs ++= loadExtra(locale, bundleName)
    inputs
  }

  protected def loadExtra(locale: Locale, bundleName: String): collection.Seq[(String, InputStream)] = {
    List.empty
  }

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

  /** Convert locale to string with language_country[_variant]
   */
  private def toLocaleStr(locale: Locale): String = {
    if (locale == Locale.ROOT) return ""
    val language = locale.getLanguage
    val country = locale.getCountry
    val variant = locale.getVariant
    if (language == "" && country == "" && variant == "") return ""
    val sb = new StringBuilder()
    if (variant != "")
      sb.append(language).append('_').append(country).append('_').append(variant)
    else if (country != "")
      sb.append(language).append('_').append(country)
    else
      sb.append(language)
    sb.toString
  }

  /** Read key value properties
   * Group by Uppercased key,and default group
   */
  protected def readBundle(input: InputStream, charset: Charset = Charsets.UTF_8): Map[String, Map[String, String]] = {
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
          if (-1 == dotIdx)
            texts.getOrElseUpdate(defaults, new collection.mutable.HashMap[String, String]).put(key, value)
          else
            texts.getOrElseUpdate(key.substring(0, dotIdx), new collection.mutable.HashMap[String, String]).put(key.substring(dotIdx + 1), value)
        } else
          texts.getOrElseUpdate(defaults, new collection.mutable.HashMap[String, String]).put(key, value)
      }
      line = reader.readLine()
    }
    if (!texts.contains(defaults)) texts.put(defaults, collection.mutable.HashMap.empty)
    val results = texts.map { case (name, values) => (name, values.toMap) }
    results.toMap
  }

}
