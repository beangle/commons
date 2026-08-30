/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.collection

/** Identity-based map similar to java.util.IdentityHashMap, using chaining buckets.
 * Grows by doubling the table when the load factor is exceeded. Does not support
 * null key or null value. Not thread-safe.
 *
 * @param capacity initial bucket count (must be even)
 */
final class IdentityMap[K <: AnyRef, V](capacity: Int = 16) {
  require(capacity >= 2 && capacity % 2 == 0, "capacity must be even and at least 2")

  private var table = new Array[Entry[K, V]](capacity)
  private var mask = capacity - 1
  private var count = 0
  private val loadFactor = 0.75

  /** IdentityHashMap 式散列：`(h << 1) - (h << 8)` 打散 identityHashCode 低位，减少碰撞。 */
  private def bucket(key: K): Int = {
    val h = System.identityHashCode(key)
    ((h << 1) - (h << 8)) & mask
  }

  /** Gets the value for the key (identity-based lookup).
   *
   * @param key the key
   * @return the value, or null if not found
   */
  final def get(key: K): V = {
    var entry = table(bucket(key))
    while (null != entry) {
      if (key eq entry.key) return entry.value
      entry = entry.next
    }
    null.asInstanceOf[V]
  }

  /** Removes all entries from the map. */
  def clear(): Unit = {
    var i = 0
    val tab = table
    while (i < tab.length) {
      tab(i) = null
      i += 1
    }
    count = 0
  }

  /** Returns true if the key exists.
   *
   * @param key the key
   * @return true if present
   */
  def contains(key: K): Boolean =
    null != get(key)

  /** Puts the key-value pair. Returns true if key already existed (replaced).
   *
   * @param key   the key
   * @param value the value
   * @return true if replaced existing, false if new
   */
  def put(key: K, value: V): Boolean = {
    val hash = bucket(key)
    val tab = table
    var entry = tab(hash)
    while (null != entry) {
      if (key eq entry.key) {
        entry.value = value
        return true
      }
      entry = entry.next
    }
    if count + 1 > (table.length * loadFactor).toInt then
      resize()
      val h = bucket(key)
      table(h) = new Entry(key, value, table(h))
    else
      table(hash) = new Entry(key, value, tab(hash))
    count += 1
    false
  }

  /** Doubles the table and rehashes all entries. */
  private def resize(): Unit = {
    val old = table
    val newTable = new Array[Entry[K, V]](old.length << 1)
    table = newTable
    mask = newTable.length - 1
    var i = 0
    while (i < old.length) {
      var e = old(i)
      while (null != e) {
        val next = e.next
        val h = bucket(e.key)
        e.next = newTable(h)
        newTable(h) = e
        e = next
      }
      i += 1
    }
  }

  /** Removes the key and returns its value.
   *
   * @param key the key to remove
   * @return the previous value, or null if not found
   */
  def remove(key: K): V = {
    val tab = table

    val hash = bucket(key)
    var e = tab(hash)
    var prev: Entry[K, V] = null
    while (null != e) {
      if (key eq e.key) {
        if (prev != null) prev.next = e.next
        else tab(hash) = e.next

        val oldValue = e.value
        e.value = null.asInstanceOf[V]
        count -= 1
        return oldValue
      }
      prev = e
      e = e.next
    }
    null.asInstanceOf[V]
  }

  /** Returns the number of entries. */
  def size(): Int = count

  /** Returns iterator over keys. */
  def keysIterator: Iterator[K] =
    new KeyIterator(table)

  class Entry[K, V](val key: K, var value: V, var next: Entry[K, V])

  class EntryIterator[K, V](table: Array[Entry[K, V]]) {
    var entry: Entry[K, V] = _
    var hasNext = false
    var index = -1

    def move(): Unit = {
      if (index < table.length)
        if (null != entry && null != entry.next)
          entry = entry.next
        else {
          entry = null
          index += 1
          while (null == entry && index < table.length) {
            entry = table(index)
            index += 1
          }
        }
      else
        entry = null
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
