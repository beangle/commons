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

import org.beangle.commons.lang.Strings
import org.w3c.dom.{Node, Element as XmlElement}

/** W3C DOM utilities. */
object Doms {

  /** Returns true if the element is the document root.
   *
   * @param elem the element to check
   * @return true if root
   */
  def isRootElement(elem: XmlElement): Boolean = {
    elem.getParentNode != null && elem.getParentNode.getNodeType == Node.DOCUMENT_NODE
  }

  /** Gets text content when element has no child elements.
   *
   * @param elem the W3C element
   * @return Some(text) if text-only, None if has element children
   */
  def getTextValue(elem: XmlElement): Option[String] = {
    val nl = elem.getChildNodes
    var hasElement = false
    for (i <- 0 until nl.getLength if !hasElement) {
      hasElement = nl.item(i).getNodeType == Node.ELEMENT_NODE
    }
    if (hasElement) None else {
      val tc = elem.getTextContent
      if (Strings.isNotBlank(tc)) Some(tc.trim) else None
    }
  }
}
