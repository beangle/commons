/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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

  /**
   * A <code>FilterChain</code> that records whether or not
   * {@link FilterChain#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse)} is
   * called.
   */
  protected class VirtualFilterChain(val originalChain: FilterChain, additionalFilters: List[_ <: Filter])
    extends FilterChain {

    private val iter: Iterator[_ <: Filter] = additionalFilters.iterator

    def doFilter(request: ServletRequest, response: ServletResponse) {
      if (iter.hasNext) iter.next.doFilter(request, response, this)
      else originalChain.doFilter(request, response)
    }
  }

  override final def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    new VirtualFilterChain(chain, getFilters(request)).doFilter(request, response)
  }

  def getFilters(request: ServletRequest): List[_ <: Filter]

}

object MatchedCompositeFilter {
  def build(filters: List[Filter]): Map[RequestMatcher, List[Filter]] = Map((new AntPathRequestMatcher("/**"), filters))
  def build(urlMap: Map[String, List[Filter]]): Map[RequestMatcher, List[Filter]] = {
    val builded = new collection.mutable.HashMap[RequestMatcher, List[Filter]]
    urlMap.foreach { e =>
      builded.put(new AntPathRequestMatcher(e._1), e._2)
    }
    builded.toMap
  }
}

import MatchedCompositeFilter.build
class MatchedCompositeFilter(val chainMap: Map[RequestMatcher, List[Filter]]) extends GenericCompositeFilter {

  def this(filters: List[Filter]) {
    this(build(filters))
  }

  def this(urlMap: Map[String, List[Filter]]) {
    this(build(urlMap))
  }
  /**
   * Returns the first filter chain matching the supplied URL.
   */
  protected override def getFilters(res: ServletRequest): List[Filter] = {

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