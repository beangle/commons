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

package org.beangle.commons.xml

import org.beangle.commons.lang.Charsets
import org.w3c.dom.{Document as XmlDocument, Element as XmlElement}

import java.io.{ByteArrayInputStream, File, FileInputStream, InputStream}
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

object Document {

  def parse(contents: String): Document = {
    parse(new ByteArrayInputStream(contents.getBytes(Charsets.UTF_8)))
  }

  def parse(url: URL): Document = {
    parse(url.openStream())
  }

  def parse(file: File): Document = {
    parse(new FileInputStream(file))
  }

  def parse(in: InputStream): Document = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(false)
    val docBuilder = factory.newDocumentBuilder()
    val doc = docBuilder.parse(in)
    in.close()
    convert(doc)
  }

  def convert(xdoc: XmlDocument): Document = {
    convertNode(xdoc.getDocumentElement).asInstanceOf[Document]
  }

  def convertNode(elem: XmlElement): Element = {
    val node = if (Doms.isRootElement(elem)) new Document(elem.getTagName) else new Element(elem.getTagName)
    val attrs = elem.getAttributes
    Doms.getTextValue(elem) foreach { t => node.inner(t) }
    (0 until attrs.getLength) foreach { i =>
      val a = attrs.item(i)
      node.set(a.getNodeName, a.getNodeValue)
    }
    val children = elem.getChildNodes
    (0 until children.getLength) foreach { i =>
      children.item(i) match {
        case c: XmlElement => node.append(convertNode(c))
        case _ =>
      }
    }
    node
  }
}

class Document(name: String) extends Element(name) {
  override def toXml: String = {
    val buf = new StringBuilder("""<?xml version="1.0" encoding="UTF-8" ?>""")
    buf.append("\n")
    appendXml(this, "", buf)
    buf.toString
  }

  def merge(o: Document): Unit = {
    o.childNodes foreach { c => this.append(c) }
  }

}
