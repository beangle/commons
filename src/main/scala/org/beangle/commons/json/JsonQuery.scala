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

package org.beangle.commons.json

import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.beta

import java.util as ju

@beta
object JsonQuery {

  def getString(data: ju.Map[String, _], path: String): String = {
    data.get(path).asInstanceOf[String]
  }

  def get(data: ju.Map[String, Object], path: String): Object = {
    val paths = Strings.split(path, ".")
    var current: Object = data
    var i = 0
    while (i < paths.length && current != null) {
      val p = paths(i)
      var index = -1
      if p.startsWith("[") then index = Strings.substringBetween(p, "[", "]").toInt
      if (index >= 0) {
        current = current.asInstanceOf[ju.List[_]].get(index)
      } else {
        current match
          case m: ju.Map[_, _] => current = m.get(p)
          case l: ju.List[_] =>
            val rl = new ju.ArrayList[Object]
            val il = l.iterator()
            while (il.hasNext) {
              val n = il.next()
              rl.add(n.asInstanceOf[ju.Map[_, _]].get(p).asInstanceOf[Object])
            }
            current = rl
      }
      i += 1
    }
    current
  }
}
