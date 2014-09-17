package org.beangle.commons.web.filter

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.Filter

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