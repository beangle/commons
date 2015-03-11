package org.beangle.commons

package object collection {
  type IdentityCache[A <: AnyRef,B <: AnyRef] = IdentityMap[A,B]
}