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
package org.beangle.commons.jdbc

import org.beangle.commons.lang.Strings
/**
 * This class maps a type to names. Associations may be marked with a capacity.
 * Calling the get() method with a type and actual size n will return the
 * associated name with smallest capacity >= n, if available and an unmarked
 * default type otherwise. Eg, setting
 * @define l $l
 * {{{
 * names.put(type, &quot;TEXT&quot;);
 * names.put(type, 255, &quot;VARCHAR($l)&quot;);
 * names.put(type, 65534, &quot;LONGVARCHAR($l)&quot;);
 * }}}
 *
 * will give you back the following:
 *
 * {{{
 * names.get(type)         // --&gt; &quot;TEXT&quot; (default)
 * names.get(type,    100) // --&gt; &quot;VARCHAR(100)&quot; (100 is in [0:255])
 * names.get(type,   1000) // --&gt; &quot;LONGVARCHAR(1000)&quot; (1000 is in [256:65534])
 * names.get(type, 100000) // --&gt; &quot;TEXT&quot; (default)
 * }}}
 *
 * On the other hand, simply putting
 *
 * {{{
 * names.put(type, &quot;VARCHAR($l)&quot;);
 * }}}
 *
 * would result in
 *
 * {{{
 * names.get(type)        // --&gt; &quot;VARCHAR($l)&quot; (will cause trouble)
 * names.get(type, 100)   // --&gt; &quot;VARCHAR(100)&quot;
 * names.get(type, 10000) // --&gt; &quot;VARCHAR(10000)&quot;
 * }}}
 *
 * @author chaostone
 */
class TypeNames {

  var weighted: Map[Int, Map[Int, String]] = Map.empty
  var defaults: Map[Int, String] = Map.empty

  /**
   * get default type name for specified type
   *
   * @param typecode the type key
   * @return the default type name associated with specified key
   */
  def get(typecode: Int) = defaults(typecode)

  /**
   * get type name for specified type and size
   *
   * @param typecode  the type key
   * @param size the SQL length
   * @param precision  the SQL precision
   * @param scale the SQL scale
   * @return the associated name with smallest capacity >= size, if available
   *         and the default type name otherwise
   */
  def get(typecode: Int, size: Int, precision: Int, scale: Int): String = {
    val map = weighted.get(typecode).orNull //Map[Int, String]
    if (map != null && map.size > 0) {
      // iterate entries ordered by capacity to find first fit
      for ((k, v) <- map) {
        if (size <= k) {
          return replace(v, size, precision, scale);
        }
      }
    }
    return replace(get(typecode), size, precision, scale);
  }

  private def replace(typeString: String, size: Int, precision: Int, scale: Int) = {
    var finalType = typeString
    finalType = Strings.replace(finalType, "$s", scale.toString())
    finalType = Strings.replace(finalType, "$l", size.toString())
    Strings.replace(finalType, "$p", precision.toString())
  }

  /**
   * set a type name for specified type key and capacity
   *
   * @param typecode
   * the type key
   */
  def put(typecode: Int, capacity: Int, value: String) {
    val map = weighted.get(typecode).getOrElse(new collection.immutable.TreeMap[Int, String]) //Map[Int, String]
    weighted += (typecode -> (map + (capacity -> value)))
  }

  /**
   * set a default type name for specified type key
   *
   * @param typecode
   * the type key
   */
  def put(typecode: Int, value: String) {
    defaults += (typecode -> value)
  }
}
