package org.beangle.commons.collection

import org.beangle.commons.bean.PropertyUtils

class Properties extends collection.mutable.HashMap[String, Any] {

  def this(obj: Object, attrs: String*) {
    this()
    for (attr <- attrs) {
      val value = PropertyUtils.getProperty[Any](obj, attr)
      if (null != value) this.put(attr, value)
    }
  }

  def add(attr: String, obj: Object, nestedAttrs: String*): Unit = {
    if (null != obj)
      put(attr, new Properties(obj, nestedAttrs: _*))
  }
}