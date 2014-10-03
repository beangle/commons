package org.beangle.commons.http.accept

import org.beangle.commons.bean.Factory
import org.beangle.commons.bean.Initializing

class ContentNegotiationManagerFactory extends Factory[ContentNegotiationManager] with Initializing {

  var favorParameter: Boolean = _
  var favorPathExtension: Boolean = _
  var ignoreAcceptHeader: Boolean = _
  var parameterName: String = _
  var result: ContentNegotiationManager = null

  override def init() {
    val resolvers = new collection.mutable.ListBuffer[ContentTypeResolver]
    if (this.favorPathExtension) resolvers += new PathExtensionContentResolver()
    if (this.favorParameter) resolvers += new ParameterContentResolver(parameterName)
    if (!this.ignoreAcceptHeader) resolvers += new HeaderContentTypeResolver()
    result = new ContentNegotiationManager(resolvers.toList)
  }
}