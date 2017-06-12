/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.cdi.spring.config

import java.io.InputStream

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.springframework.core.io.Resource
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

/**
 * BeanDefinitionReader class.
 *
 * @author chaostone
 */
class BeanDefinitionReader {

  /**
   * load.
   */
  def load(resource: Resource): List[ReconfigBeanDefinitionHolder] = {
    val holders = new collection.mutable.ListBuffer[ReconfigBeanDefinitionHolder]
    try {
      val inputStream = resource.getInputStream()
      try {
        val inputSource = new InputSource(inputStream)
        val factory = DocumentBuilderFactory.newInstance()
        val docBuilder = factory.newDocumentBuilder()
        val doc = docBuilder.parse(inputSource)
        val root = doc.getDocumentElement()
        val nl = root.getChildNodes()
        val parser = new BeanDefinitionParser()
        for (i <- 0 until nl.getLength) {
          val node = nl.item(i)
          if (node.isInstanceOf[Element]) {
            val ele = node.asInstanceOf[Element]
            holders += parser.parseBeanDefinitionElement(ele)
          }
        }
      } finally {
        if (null != inputStream) inputStream.close()
      }
    } catch {
      case ex: Exception => throw new RuntimeException("IOException parsing XML document from " + resource.getDescription(), ex)
    }
    holders.toList
  }
}
