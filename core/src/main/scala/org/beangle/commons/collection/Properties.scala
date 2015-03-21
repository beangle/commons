package org.beangle.commons.collection

import org.beangle.commons.bean.Properties

class Properties extends collection.mutable.HashMap[String, Any] {

  def this(obj: Object, attrs: String*) {
    this()
    for (attr <- attrs) {
      val idx = attr.indexOf("->")
      if (-1 == idx) {
        val value = Properties.get[Any](obj, attr)
        if (null != value) this.put(attr, value)
      } else {
        val value = Properties.get[Any](obj, attr.substring(0, idx))
        if (null != value) this.put(attr.substring(idx + 2), value)
      }
    }
  }

  def add(attr: String, obj: Object, nestedAttrs: String*): Unit = {
    if (null != obj)
      put(attr, new Properties(obj, nestedAttrs: _*))
  }
}