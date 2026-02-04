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

import org.beangle.commons.concurrent.Locks
import org.beangle.commons.io.IOs.using
import org.beangle.commons.io.Resources
import org.beangle.commons.xml.Document

import java.net.URL
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.xml.parsers.DocumentBuilderFactory

object XmlConfigs {
  private var configs: Map[String, Document] = Map.empty
  //读写控制，多个读写者，读不到可以写
  private val rwLock = new ReentrantReadWriteLock()

  def clear(): Unit = {
    configs = Map.empty
  }

  def load(path: String): Document = {
    Locks.withReadLock(rwLock) {
      configs.get(path)
    } match {
      case Some(d) => d
      case None =>
        Locks.withWriteLock(rwLock) {
          //二次读取（防止并发重复生成）
          configs.get(path) match {
            case Some(d) => d
            case None =>
              val urls = Resources.load(path)
              val newDoc = parseConfig(urls)
              configs += (path -> newDoc)
              newDoc
          }
        }
    }
  }

  def parseConfig(urls: Iterable[URL]): Document = {
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
