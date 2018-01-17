/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
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
package org.beangle.commons.web.filter

import javax.servlet.{ Filter, FilterChain, ServletRequest, ServletResponse }
import javax.servlet.http.HttpServletRequest

abstract class GenericCompositeFilter extends GenericHttpFilter {

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    new VirtualFilterChain(chain, getFilters(request).iterator).doFilter(request, response)
  }

  def getFilters(request: ServletRequest): List[_ <: Filter]

}

object MatchedCompositeFilter {
  def build(filters: List[Filter]): Map[String, List[Filter]] = Map(("/**", filters))
  def build(urlMap: Map[String, List[Filter]]): Map[RequestMatcher, List[Filter]] = {
    val builded = new collection.mutable.HashMap[RequestMatcher, List[Filter]]
    urlMap.foreach(e => builded.put(new AntPathRequestMatcher(e._1, null), e._2))
    builded.toMap
  }
}

class MatchedCompositeFilter(urlMap: Map[String, List[Filter]]) extends GenericCompositeFilter {

  val chainMap: Map[RequestMatcher, List[Filter]] = MatchedCompositeFilter.build(urlMap)

  def this(filters: List[Filter]) {
    this(MatchedCompositeFilter.build(filters))
  }

  /**
   * Returns the first filter chain matching the supplied URL.
   */
  override def getFilters(res: ServletRequest): List[Filter] = {
    val request = res.asInstanceOf[HttpServletRequest]
    chainMap.find(v => v._1.matches(request)) match {
      case Some(v) => v._2
      case None => List.empty
    }
  }

  override def toString(): String = {
    val sb = new StringBuffer()
    sb.append("MatcherCompositeFilter[").append("Filter Chains: ").append(chainMap).append(']')
    sb.toString()
  }

}