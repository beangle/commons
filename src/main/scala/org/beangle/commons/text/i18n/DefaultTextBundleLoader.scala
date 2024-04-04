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
import org.beangle.commons.lang.{Charsets, ClassLoaders, Strings}

import java.io.{InputStream, InputStreamReader, LineNumberReader}
import java.nio.charset.Charset
import java.util.Locale

class DefaultTextBundleLoader extends TextBundleLoader {
  override def find(locale: Locale, bundleName: String): collection.Seq[(String, InputStream)] = {
    val inputs = Collections.newBuffer[(String, InputStream)]
    val fullName = bundleName.replace('.', '/')
    val localeName = toLocaleStr(locale)
    if ("" == localeName) {
      //find java properties(utf-8)
      ClassLoaders.getResource(s"${fullName}.properties") foreach { b =>
        inputs.addOne((b.toString, b.openStream()))
      }
    } else {
      //find java properties(utf-8)
      ClassLoaders.getResource(s"${fullName}_${localeName}.properties") foreach { b =>
        inputs.addOne((b.toString, b.openStream()))
      }
      //Generate name like bundleName.zh_CN
      ClassLoaders.getResource(s"${fullName}.${localeName}") foreach { b =>
        inputs.addOne((b.toString, b.openStream()))
      }
    }
    inputs ++= findExtra(locale, bundleName)
    inputs
  }

  protected def findExtra(locale: Locale, bundleName: String): collection.Seq[(String, InputStream)] = {
    List.empty
  }

  override def load(locale: Locale, bundleName: String): Map[String, TextBundle] = {
    val results = Collections.newMap[String, TextBundle]
    var curName = bundleName
    var bundles = this.find(locale, curName)

    if (bundles.isEmpty) {
      val dotIdx = curName.lastIndexOf('.')
      if (dotIdx > 0 && dotIdx < curName.length - 1 && Character.isUpperCase(curName.charAt(dotIdx + 1))) {
        curName = curName.substring(0, dotIdx) + ".package"
        bundles = this.find(locale, curName)
      }
    }

    if (bundles.isEmpty) {
      results.put(bundleName, new TextBundle(locale, curName, Map.empty))
    } else {
      val prefix = Strings.substringBeforeLast(curName, ".") + "."

      bundles foreach { b =>
        this.resolve(b._2) foreach {
          case (name, values) =>
            val key = if name.isEmpty then curName else prefix + name
            results.get(key) match
              case None => results.put(key, new TextBundle(locale, b._1, values))
              case Some(existed) => results.put(key, existed.merge(new TextBundle(locale, b._1, values)))
        }
        if !results.contains(bundleName) then
          results.put(bundleName, new TextBundle(locale, bundleName, Map.empty))
      }
    }
    results.toMap
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
  protected def resolve(input: InputStream, charset: Charset = Charsets.UTF_8): Map[String, Map[String, String]] = {
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
