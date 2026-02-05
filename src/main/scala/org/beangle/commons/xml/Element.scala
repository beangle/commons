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

object Element {
  def apply(name: String, attrs: (String, String)*): Element = {
    val node = new Element(name)
    node.set(attrs)
    node
  }
}

class Element(val label: String) extends Node {
  protected[xml] val attributes = Collections.newMap[String, String]
  protected[xml] val childNodes = Collections.newBuffer[Element]
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

  def set(kvs: Iterable[(String, String)]): Unit = {
    attributes.addAll(kvs)
  }

  def set(key: String, value: Option[String]): Unit = {
    value foreach { v => attributes += (key -> v) }
  }

  def set(key: String, value: String): Unit = {
    attributes += (key -> value)
  }

  def append(node: Node): Unit = {
    node match {
      case elem: Element => childNodes.addOne(elem)
      case _ => throw new IllegalArgumentException(s"cannot append ${node.getClass.getName} to elments.")
    }
  }

  def append(name: String, attrs: (String, String)*): Element = {
    val node = new Element(name)
    node.set(attrs)
    childNodes += node
    node
  }

  override def toString: String = {
    s"<$label>...</$label>"
  }

  protected def appendXml(node: Element, indent: String, buf: mutable.StringBuilder): Unit = {
    buf.append(indent).append(s"<${node.label}")
    node.attributes foreach { case (k, v) =>
      buf ++= s""" $k="${XmlEscaper.escape(v)}""""
    }
    node.innerText match {
      case None =>
        if (node.childNodes.isEmpty) {
          buf ++= "/>\n"
        } else {
          buf ++= ">\n"
          node.childNodes foreach (appendXml(_, "  " + indent, buf))
          buf.append(indent).append(s"</${node.label}>\n")
        }
      case Some(t) =>
        if (node.children.isEmpty) { //只有文本的节点
          buf.append(">").append(XmlEscaper.escapeText(t)).append(s"</${node.label}>\n")
        } else { //文本和子节点都有的情况
          buf.append(">\n")
          buf.append(indent).append(XmlEscaper.escapeText(t)).append("\n")
          buf.append(indent).append(s"</${node.label}>\n")
        }
    }
  }

  def inner(text: String): this.type = {
    this.innerText = Some(text)
    this
  }
}
