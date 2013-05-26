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
package org.beangle.commons.entity.dao

import org.beangle.commons.collection.page.PageLimit

/**
 * <p>
 * QueryBuilder interface.
 * </p>
 * 
 * @author chaostone
 * @version $Id: $
 */
trait QueryBuilder[T] {

  /**
   * <p>
   * build.
   * </p>
   * 
   * @return a {@link org.beangle.commons.dao.query.Query} object.
   */
  def build():Query[T]

  /**
   * <p>
   * limit.
   * </p>
   * 
   * @param limit a {@link org.beangle.commons.collection.page.PageLimit} object.
   * @return a {@link org.beangle.commons.dao.query.QueryBuilder} object.
   */
  def limit(limit:PageLimit):  QueryBuilder[T]

  /**
   * <p>
   * getParams.
   * </p>
   * 
   * @return a {@link java.util.Map} object.
   */
  def params:  Map[String, Any]

  /**
   * <p>
   * params.
   * </p>
   * 
   * @param newParams a {@link java.util.Map} object.
   * @return a {@link org.beangle.commons.dao.query.QueryBuilder} object.
   */
  def params(newParams:Map[String, Any]):this.type
}
