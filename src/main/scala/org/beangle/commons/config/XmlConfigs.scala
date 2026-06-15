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

import org.beangle.commons.xml.Document

object XmlConfigs {
  val GlobalConfigKey = "beangle.config.xmlconfigs"
}

/** XML config document loader. */
class XmlConfigs {
  private var configs: Map[String, Document] = Map.empty

  def load(path: String): Document = {
    configs.get(path) match {
      case Some(d) => d
      case None =>
        XmlDocs.load(path) match {
          case Some(doc) =>
            configs += (path -> doc)
            doc
          case None => new Document("missing")
        }
    }
  }

  def clear(): Unit = {
    configs = Map.empty
  }
}
