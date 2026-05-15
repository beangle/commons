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

import org.beangle.commons.collection.Collections
import org.beangle.commons.text.escape.XmlEscaper

import scala.collection.mutable

/** Element factory. */
object Element {

  /** Creates an Element with name and attributes. */
  def apply(name: String, attrs: (String, String)*): Element = {
    val node = new Element(name)
    node.set(attrs)
    node
  }
}

class Element(val label: String) extends Node {
  protected[xml] val attributes = new mutable.LinkedHashMap[String, String]
  protected[xml] val childNodes = Collections.newBuffer[Node]
  protected[xml] var innerText: Option[String] = None

  override def children: collection.Seq[Node] = {
    childNodes
  }

  def toXml: String = {
    val buf = new StringBuilder()
    appendXml(this, "", buf)
    buf.toString
  }

  override def text: String = innerText.getOrElse("")

  override def get(name: String): Option[String] = {
    attributes.get(name)
  }

  override def attrs: collection.Map[String, String] = {
    attributes
  }

  /** Sets multiple attributes. */
  def set(kvs: Iterable[(String, String)]): Unit = {
    attributes.addAll(kvs)
  }

  /** Sets attribute; removes if value is None. */
  def set(key: String, value: Option[String]): Unit = {
    value foreach { v => attributes += (key -> v) }
  }

  /** Sets attribute. */
  def set(key: String, value: String): Unit = {
    attributes += (key -> value)
  }

  /** Appends an Element child. */
  def append(node: Node): Unit = {
    childNodes.addOne(node)
  }

  /** Creates and appends child Element; returns the new child. */
  def append(name: String, attrs: (String, String)*): Element = {
    val node = new Element(name)
    node.set(attrs)
    childNodes += node
    node
  }

  override def toString: String = {
    s"<$label>...</$label>"
  }

  protected def appendXml(text: Text, indent: String, buf: mutable.StringBuilder): Unit = {
    buf.append(indent).append(XmlEscaper.escapeText(text.text)).append("\n")
  }

  protected def appendXml(elem: Element, indent: String, buf: mutable.StringBuilder): Unit = {
    buf.append(indent).append(s"<${elem.label}")
    elem.attributes foreach { case (k, v) =>
      buf ++= s""" $k="${XmlEscaper.escape(v)}""""
    }
    elem.innerText match {
      case None =>
        if (elem.childNodes.isEmpty) {
          buf ++= "/>\n"
        } else {
          buf ++= ">\n"
          elem.childNodes foreach {
            case e: Element => appendXml(e, "  " + indent, buf)
            case t: Text => appendXml(t, "  " + indent, buf)
          }
          buf.append(indent).append(s"</${elem.label}>\n")
        }
      case Some(t) =>
        if (elem.children.isEmpty) { // text-only node
          buf.append(">").append(XmlEscaper.escapeText(t)).append(s"</${elem.label}>\n")
        } else { // node with both text and child elements
          buf.append(">\n")
          buf.append(indent).append(XmlEscaper.escapeText(t)).append("\n")
          buf.append(indent).append(s"</${elem.label}>\n")
        }
    }
  }

  /** Sets text content; returns this for chaining. */
  def inner(text: String): this.type = {
    this.innerText = Some(text)
    this
  }
}
