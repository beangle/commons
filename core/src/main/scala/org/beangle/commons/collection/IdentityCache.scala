package org.beangle.commons.collection

/**
 * Similar to java.util.IdentityHashMap,but using chaining bucket
 * But do not support null key and null value
 */
class IdentityCache[K <: AnyRef, V <: AnyRef](capacity: Int = 1024) {
  assert(capacity % 2 == 0)

  private val table = new Array[Entry[K, V]](capacity)
  private val mask = capacity - 1

  final def get(key: K): V = {
    val bucket = System.identityHashCode(key) & mask
    var entry = table(bucket)
    while (null != entry) {
      if (key eq entry.key) return entry.value
      entry = entry.next
    }
    null.asInstanceOf[V]
  }

  final def contains(key: K): Boolean = {
    null != get(key)
  }

  final def put(key: K, value: V): Boolean = {
    val hash = System.identityHashCode(key) & mask
    val tab = table
    var entry = tab(hash)
    while (null != entry) {
      if (key eq entry.key) {
        entry.value = value
        return true
      }
      entry = entry.next
    }
    tab(hash) = new Entry(key, value, tab(hash))
    false
  }

  def remove(key: K): V = {
    val tab = table

    val hash = System.identityHashCode(key) & mask
    var e = tab(hash)
    var prev: Entry[K, V] = null
    while (null != e) {
      if (key eq e.key) {
        if (prev != null) prev.next = e.next
        else tab(hash) = e.next

        val oldValue = e.value
        e.value = null.asInstanceOf[V]
        return oldValue
      }
      prev = e
      e = e.next
    }
    null.asInstanceOf[V]
  }

  final def size(): Int = {
    var size = 0
    (0 until table.length) foreach { bucket =>
      var entry = table(bucket)
      while (null != entry) {
        size += 1
        entry = entry.next
      }
    }
    size
  }

  final class Entry[K, V](val key: K, var value: V, var next: Entry[K, V])

}