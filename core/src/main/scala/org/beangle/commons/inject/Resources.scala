/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.inject

import java.net.URL
import scala.collection.mutable.ListBuffer

/**
 * <p>
 * ConfigResource class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class Resources {

  protected var global: URL = _

  protected var locals: List[URL]=_

  protected var user: URL = _

  /**
   * getAllPaths.
   */
  def getAllPaths(): List[URL] = {
    val all = new ListBuffer[URL]
    if (null != global) all += global
    if (null != locals) all ++= locals
    if (null != user) all += user
    all.toList
  }

  /**
   * Return true is empty
   */
  def isEmpty(): Boolean = {
    null == global && null == user && (null == locals || locals.isEmpty)
  }

  /**
   * Getter for the field <code>global</code>.
   */
  def getGlobal(): URL = global

  /**
   * Setter for the field <code>global</code>.
   */
  def setGlobal(first: URL) {
    this.global = first
  }

  /**
   * Getter for the field <code>locals</code>.
   */
  def getLocals(): List[URL] = locals

  /**
   * Setter for the field <code>locals</code>.
   */
  def setLocals(paths: List[URL]) {
    this.locals = paths
  }

  /**
   * Getter for the field <code>user</code>.
   */
  def getUser(): URL = user

  /**
   * Setter for the field <code>user</code>.
   */
  def setUser(last: URL) {
    this.user = last
  }
}
