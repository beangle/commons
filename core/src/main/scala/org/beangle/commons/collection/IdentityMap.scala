/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
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
package org.beangle.commons.collection

/**
 * Similar to java.util.IdentityHashMap,but using chaining bucket
 * But do not support null key and null value
 * (not thread safe)
 * @since 4.2.3
 */
final class IdentityMap[K <: AnyRef, V](capacity: Int = 1024) {
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

  def clear(): Unit = {
    var i = 0
    val tab = table
    while (i < tab.length) {
      tab(i) = null
      i += 1
    }
  }
  def contains(key: K): Boolean = {
    null != get(key)
  }

  def put(key: K, value: V): Boolean = {
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

  def size(): Int = {
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

  def keysIterator: Iterator[K] = {
    new KeyIterator(table)
  }

  class Entry[K, V](val key: K, var value: V, var next: Entry[K, V])

  class EntryIterator[K, V](table: Array[Entry[K, V]]) {
    var entry: Entry[K, V] = _
    var hasNext = false
    var index = -1

    def move() {
      if (index < table.length) {
        if (null != entry && null != entry.next) {
          entry = entry.next
        } else {
          entry = null
          index += 1
          while (null == entry && index < table.length) {
            entry = table(index)
            index += 1
          }
        }
      } else {
        entry = null
      }
      hasNext = (entry != null)
    }
  }

  class KeyIterator[K](table: Array[Entry[K, V]]) extends EntryIterator(table) with Iterator[K] {

    move()

    override def next(): K = {
      val key = entry.key
      move()
      key
    }
  }
}
