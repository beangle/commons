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

/** W3C Document parsing factory. */
object Document {

  /** Parses XML from a string. */
  def parse(contents: String): Document = {
    parse(new ByteArrayInputStream(contents.getBytes(Charsets.UTF_8)))
  }

  /** Parses XML from a URL. */
  def parse(url: URL): Document = {
    parse(url.openStream())
  }

  /** Parses XML from a file. */
  def parse(file: File): Document = {
    parse(new FileInputStream(file))
  }

  /** Parses XML from an InputStream. Closes the stream after read. */
  def parse(in: InputStream): Document = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(false)
    val docBuilder = factory.newDocumentBuilder()
    val doc = docBuilder.parse(in)
    in.close()
    convert(doc)
  }

  /** Converts a W3C Document to this Document model. */
  def convert(xdoc: XmlDocument): Document = {
    convertNode(xdoc.getDocumentElement).asInstanceOf[Document]
  }

  /** Converts a W3C Element to an Element. */
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

  /** Merges o's child elements into this document. */
  def merge(o: Document): Unit = {
    o.childNodes foreach { c => this.append(c) }
  }

}
