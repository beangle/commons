/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.data.dao

import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.lang.Strings

/**
 * <p>
 * QueryBean class.
 * </p>
 *
 * @author chaostone
 */
class QueryBean[T] extends LimitQuery[T] {

  var lang: Query.Lang = _

  var statement: String = _

  var countStatement: String = _

  var limit: PageLimit = _

  var cacheable: Boolean = false

  var params: Map[String, Any] = _

  /**
   * Returns count query {@link org.beangle.commons.dao.query.Query}.
   */
  def countQuery: Query[T] = {
    if (Strings.isEmpty(countStatement)) return null
    val bean = new QueryBean[T]();
    bean.statement = countStatement
    bean.lang = lang
    bean.params = params
    bean.cacheable = cacheable
    bean
  }

  /** {@inheritDoc} */
  def limit(limit: PageLimit): LimitQuery[T] = {
    this.limit = limit
    this
  }
}
