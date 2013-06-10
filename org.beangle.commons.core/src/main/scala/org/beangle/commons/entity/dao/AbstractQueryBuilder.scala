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
 * 抽象查询
 * 
 * @author chaostone
 * @version $Id: $
 */
abstract class AbstractQueryBuilder[T] extends QueryBuilder[T] {

  /** query 查询语句 */
  protected var queryStr:String

  /** count 计数语句 */
  protected var countStr:String

  /** 分页 */
  protected var limit:PageLimit

  /** 参数 */
  var params:Map[String, Any]

  /** 缓存查询结果 */
  protected var cacheable = false;

  /**
   * <p>
   * toQueryString.
   * </p>
   * 
   * @return a {@link java.lang.String} object.
   */
  def  toQueryString:String

  /**
   * <p>
   * toCountString.
   * </p>
   * 
   * @return a {@link java.lang.String} object.
   */
  def toCountString:String
}
