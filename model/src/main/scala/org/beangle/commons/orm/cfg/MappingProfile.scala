package org.beangle.commons.orm.cfg

import org.beangle.commons.lang.Strings.isNotEmpty
import org.beangle.commons.orm.NamingPolicy

class MappingProfile {
  var packageName: String = _
  var naming: NamingPolicy = _
  var _schema: String = _
  var _prefix: String = _
  var parent: MappingProfile = _

  def schema: Option[String] = {
    if (isNotEmpty(_schema)) Some(_schema)
    else if (null != parent) parent.schema
    else None
  }
  def prefix: String = {
    if (isNotEmpty(_prefix)) _prefix
    else if (null != parent) parent.prefix
    else ""
  }

  val _annotations = new collection.mutable.ListBuffer[AnnotationModule]

  def annotations: collection.Seq[AnnotationModule] = {
    if (_annotations.isEmpty && null != parent) parent._annotations
    else _annotations
  }

  override def toString(): String = {
    val sb = new StringBuilder()
    sb.append("[package:").append(packageName).append(", schema:").append(_schema)
    sb.append(", prefix:").append(_prefix).append(']')
    sb.toString()
  }
}

class AnnotationModule(val clazz: Class[_], val value: String) {
  var schema: String = _
  var prefix: String = _
}
