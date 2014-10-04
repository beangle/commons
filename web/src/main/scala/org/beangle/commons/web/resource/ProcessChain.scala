package org.beangle.commons.web.resource

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.ArrayList
import scala.collection.mutable.ArrayBuffer
import org.beangle.commons.lang.Arrays
import org.beangle.commons.io.IOs
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import java.io.ByteArrayOutputStream

class ProcessChain(filters: Iterator[ResourceFilter]) {

  def process(context: ProcessContext, request: HttpServletRequest, response: HttpServletResponse) {
    if (filters.hasNext) {
      filters.next().filter(context, request, response, this)
    }
  }
}