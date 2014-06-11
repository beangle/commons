package org.beangle.commons.web.resource

trait PathResolver {
  def resolve(name: String): List[String]
}