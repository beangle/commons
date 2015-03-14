package org.beangle.commons.collection

/**
 * Set using IdentityMap(not thread safe)
 * @since 4.2.3
 */
class IdentitySet[A <: AnyRef] extends scala.collection.mutable.Set[A] {
  val map = new IdentityMap[A, A]

  def iterator: Iterator[A] = {
    map.keysIterator
  }

  def contains(elem: A): Boolean = {
    map.contains(elem)
  }

  def -=(elem: A): this.type = {
    map.remove(elem)
    this
  }

  def +=(elem: A): this.type = {
    map.put(elem, elem)
    this
  }
}