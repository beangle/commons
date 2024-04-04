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

import org.beangle.commons.lang.{Charsets, Strings}
import org.beangle.commons.net.http.HttpUtils
import org.beangle.commons.text.i18n.DefaultTextBundleLoader

import java.io.{ByteArrayInputStream, InputStream}
import java.util.Locale

class HttpTextBundleLoader(url: String, preload: Boolean = false) extends DefaultTextBundleLoader {

  private var bundles: Set[String] = _

  if preload then loadList()

  override protected def findExtra(locale: Locale, bundleName: String): collection.Seq[(String, InputStream)] = {
    val path = s"${bundleName.replace('.', '/')}.${locale.toString}"
    if null == bundles then loadList()
    if (bundles.contains(path)) {
      val res = HttpUtils.getText(getURL(path))
      if (res.isOk) {
        List((bundleName + "@http", new ByteArrayInputStream(res.getText.getBytes(Charsets.UTF_8))))
      } else {
        List.empty
      }
    } else {
      List.empty
    }
  }

  private def loadList(): Unit = {
    val res = HttpUtils.getText(getURL("ls"))
    this.bundles = if res.isOk then Strings.split(res.getText).toSet else Set.empty
  }

  private def getURL(name: String): String = {
    Strings.replace(url, "{path}", name)
  }
}
