package org.beangle.commons.http.accept

import org.beangle.commons.bean.Factory

class ContentNegotiationManagerFactory extends Factory[ContentNegotiationManager] {

  var favorParameter: Boolean = _
  var favorPathExtension: Boolean = _
  var ignoreAcceptHeader: Boolean = _
  var parameterName: String = _
  var result: ContentNegotiationManager = null

}