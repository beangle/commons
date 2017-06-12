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

import org.beangle.commons.lang.Strings

object ProfileMatcher {
  class Node(val name: String, var parent: Node = null) {
    override def toString: String = name
    var children = new collection.mutable.ListBuffer[Node]
    if (null != parent) parent.children += this
  }
  def parse(profiles: String): Node = {
    val root = new Node("_root")
    var i = 0;
    var last = 0;
    var current: Node = root
    var commonCnt = 0
    while (i < profiles.length) {
      profiles.charAt(i) match {
        case '(' =>
          commonCnt = commonCnt + 1
          if (last == i) throw new RuntimeException(s"Illegal format$profiles: ( should follow profile name")
          current = new Node(profiles.substring(last, i).trim, current)
          last = i + 1
        case ')' =>
          commonCnt = commonCnt - 1;
          if (last != i) new Node(profiles.substring(last, i).trim, current)
          current = current.parent
          last = i + 1
        case ',' =>
          if (commonCnt > 0 && last != i) new Node(profiles.substring(last, i).trim, current)
          last = i + 1
        case _ =>
      }
      i = i + 1
    }
    new Node(profiles.substring(last, i).trim, current)
    root
  }
}

class ProfileMatcher(profiles: String) {

  var root = ProfileMatcher.parse(profiles)

  def matches(targets: Set[String]): Boolean = {
    root.children exists (child => evaluate(child, targets))
  }

  def matches(target: String): Boolean = {
    val targets = Strings.split(target, ",").map(s => s.trim).toSet
    root.children exists (child => evaluate(child, targets))
  }
  def evaluate(node: ProfileMatcher.Node, targets: Set[String]): Boolean = {
    if (targets.contains(node.name)) {
      if (node.children.isEmpty) true
      else node.children.exists(child => evaluate(child, targets))
    } else false
  }
}

