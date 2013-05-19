/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect

import java.util.Map
import java.util.TreeMap
import collection.JavaConversions._
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings

/**
 * This class maps a type to names. Associations may be marked with a capacity.
 * Calling the get() method with a type and actual size n will return the
 * associated name with smallest capacity >= n, if available and an unmarked
 * default type otherwise. Eg, setting
 *
 * <pre>
 * names.put(type, &quot;TEXT&quot;);
 * names.put(type, 255, &quot;VARCHAR($l)&quot;);
 * names.put(type, 65534, &quot;LONGVARCHAR($l)&quot;);
 * </pre>
 *
 * will give you back the following:
 *
 * <pre>
 * names.get(type)         // --&gt; &quot;TEXT&quot; (default)
 * names.get(type,    100) // --&gt; &quot;VARCHAR(100)&quot; (100 is in [0:255])
 * names.get(type,   1000) // --&gt; &quot;LONGVARCHAR(1000)&quot; (1000 is in [256:65534])
 * names.get(type, 100000) // --&gt; &quot;TEXT&quot; (default)
 * </pre>
 *
 * On the other hand, simply putting
 *
 * <pre>
 * names.put(type, &quot;VARCHAR($l)&quot;);
 * </pre>
 *
 * would result in
 *
 * <pre>
 * names.get(type)        // --&gt; &quot;VARCHAR($l)&quot; (will cause trouble)
 * names.get(type, 100)   // --&gt; &quot;VARCHAR(100)&quot;
 * names.get(type, 10000) // --&gt; &quot;VARCHAR(10000)&quot;
 * </pre>
 *
 * @author chaostone
 */
class TypeNames {

  var weighted = CollectUtils.newHashMap[Int, Map[Int, String]]
  var defaults = CollectUtils.newHashMap[Int, String]

  /**
   * get default type name for specified type
   *
   * @param typecode
   * the type key
   * @return the default type name associated with specified key
   */
  def get(typecode: Int) = {
    val result = defaults.get(typecode);
    if (result == null) throw new RuntimeException("No Dialect mapping for JDBC type: " + typecode);
    result
  }

  /**
   * get type name for specified type and size
   *
   * @param typecode
   * the type key
   * @param size
   * the SQL length
   * @param precision
   * the SQL precision
   * @param scale
   * the SQL scale
   * @return the associated name with smallest capacity >= size, if available
   *         and the default type name otherwise
   */
  def get(typecode: Int, size: Int, precision: Int, scale: Int): String = {
    val map = weighted.get(typecode); //Map[Int, String]
    if (map != null && map.size() > 0) {
      // iterate entries ordered by capacity to find first fit
      for (entry <- map.entrySet()) {
        if (size <= entry.getKey()) {
          return replace(entry.getValue(), size, precision, scale);
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
    var map = weighted.get(typecode); //Map[Int, String]
    if (map == null) {
      // add new ordered map
      map = new TreeMap[Int, String]();
      weighted.put(typecode, map);
    }
    map.put(capacity, value);
  }

  /**
   * set a default type name for specified type key
   *
   * @param typecode
   * the type key
   */
  def put(typecode: Int, value: String) {
    defaults.put(typecode, value);
  }
}
