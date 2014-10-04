package org.beangle.commons.web.filter

import javax.servlet.{ Filter, FilterChain, ServletRequest, ServletResponse }

/**
 * A <code>FilterChain</code> that records whether or not
 * {@link FilterChain#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse)} is
 * called.
 */
class VirtualFilterChain(val originalChain: FilterChain, val filterIter: Iterator[_ <: Filter]) extends FilterChain {

  def doFilter(request: ServletRequest, response: ServletResponse) {
    if (filterIter.hasNext) filterIter.next.doFilter(request, response, this)
    else originalChain.doFilter(request, response)
  }
}