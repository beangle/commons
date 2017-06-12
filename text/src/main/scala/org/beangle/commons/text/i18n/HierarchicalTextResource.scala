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
package org.beangle.commons.text.i18n

import java.{ util => ju }

import scala.language.existentials

import org.beangle.commons.lang.Strings.substringBeforeLast

class HierarchicalTextResource(clazz: Class[_], locale: ju.Locale, registry: TextBundleRegistry, formater: TextFormater)
  extends DefaultTextResource(locale, registry, formater) {

  protected override def get(key: String): Option[String] = {
    val message = findMessage(clazz, key, new collection.mutable.HashSet[String])
    if (null == message) None else Some(message)
  }

  /**
   * <li>Look for message in aClass' class hierarchy.
   * <ol>
   * <li>Look for the message in a resource bundle for aClass</li>
   * <li>If not found, look for the message in a resource bundle for any implemented interface</li>
   * <li>If not found, traverse up the Class' hierarchy and repeat from the first sub-step</li>
   * </ol>
   * </li>
   *
   * @param clazz
   * @param key
   * @param checked
   * @return
   */
  protected final def findMessage(clazz: Class[_], key: String, checked: collection.mutable.Set[String]): String = {
    val className = clazz.getName

    if (checked.contains(className)) return null
    checked.add(className)

    var msg = getClassMessage(className, key)
    if (null != msg) return msg

    // check my package
    msg = findPackageMessage(className, key, checked)
    if (null != msg) return msg

    // check all interfaces class and package
    val interfaces = new collection.mutable.HashSet[Class[_]]
    collectInterfaces(clazz, interfaces)
    for (ifc <- interfaces) {
      msg = getClassMessage(ifc.getName(), key)
      if (msg != null) return msg
    }
    for (ifc <- interfaces) {
      msg = this.findPackageMessage(ifc.getName(), key, checked)
      if (null != msg) return msg
    }

    // traverse up hierarchy
    if (clazz.isInterface()) {
      for (ifc <- clazz.getInterfaces()) {
        msg = findMessage(ifc, key, checked)
        if (null != msg) return msg
      }
    } else {
      val superClass = clazz.getSuperclass()
      if (!superClass.equals(classOf[Object]) && !clazz.isPrimitive()) {
        msg = findMessage(superClass, key, checked)
        if (null != msg) return msg
      }
    }
    null
  }

  private def collectInterfaces(me: Class[_], interfaces: collection.mutable.Set[Class[_]]): Unit = {
    for (ifc <- me.getInterfaces()) {
      if (!ifc.getName.startsWith("java.") && !ifc.getName.startsWith("scala.")) interfaces.add(ifc)
      collectInterfaces(ifc, interfaces)
    }
  }

  protected final def findPackageMessage(className: String, key: String, checkedSet: collection.mutable.Set[String]): String = {
    var msg: String = null
    var packageName = className
    var checked = false
    while (packageName.lastIndexOf('.') != -1 && !checked) {
      packageName = substringBeforeLast(packageName, ".")
      if (checkedSet.contains(packageName)) checked = true
      else {
        checkedSet.add(packageName)
        msg = getPackageMessage(packageName, key)
        if (null != msg) return msg
      }
    }
    null
  }

  /**
   * Gets the message from the named resource bundle.
   */
  protected final def getPackageMessage(packageName: String, key: String): String = {
    val bundle = registry.load(locale, packageName + ".package")
    if (null != bundle) bundle.get(key).orNull else null
  }

  /**
   * Gets the message from the named resource bundle.
   */
  protected final def getClassMessage(className: String, key: String): String = {
    registry.load(locale, substringBeforeLast(className, ".") + ".package")
    val bundle = registry.load(locale, className)
    if (null != bundle) bundle.get(key).orNull else null
  }
}
