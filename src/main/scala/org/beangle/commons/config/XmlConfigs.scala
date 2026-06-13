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

package org.beangle.commons.config

import org.beangle.commons.io.IOs.using
import org.beangle.commons.io.Resources
import org.beangle.commons.xml.Document

import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

/** Cached XML config document loader. */
object XmlConfigs {
  /** Loads XML config by path (cached); resolves via Resources.load.
   *
   * @param path the config path (e.g. classpath resource)
   * @return the parsed Document
   */
  def load(path: String): Document = {
    val urls = Resources.load(path)
    parseConfig(urls)
  }

  /** Parses XML from URLs and merges into a single Document.
   *
   * @param urls the config URLs (first is base, rest are merged)
   * @return merged Document
   */
  private def parseConfig(urls: Iterable[URL]): Document = {
    if (urls.isEmpty) {
      new Document("missing")
    } else {
      val factory = DocumentBuilderFactory.newInstance()
      factory.setNamespaceAware(false)
      val docBuilder = factory.newDocumentBuilder()
      val doc = using(urls.head.openStream()) { is => Document.convert(docBuilder.parse(is)) }
      urls.tail foreach { url =>
        val o = using(url.openStream()) { is => Document.convert(docBuilder.parse(is)) }
        doc.merge(o)
      }
      doc
    }
  }
}
