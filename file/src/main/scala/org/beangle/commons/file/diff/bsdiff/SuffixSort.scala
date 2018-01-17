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
package org.beangle.commons.file.diff.bsdiff

object SuffixSort {
  case class SearchResult(length: Int, position: Int)

  def qsufsort(I: Array[Int], V: Array[Int], data: Array[Byte]): Unit = {
    val buckets = new Array[Int](256)
    var i = 0

    while (i < data.length) {
      buckets(data(i) & 0xFF) += 1
      i += 1
    }

    i = 1
    while (i < 256) {
      buckets(i) += buckets(i - 1)
      i += 1
    }

    i = 255
    while (i > 0) {
      buckets(i) = buckets(i - 1)
      i -= 1
    }

    buckets(0) = 0

    i = 0
    while (i < data.length) {
      val idx = data(i) & 0xFF
      buckets(idx) += 1
      I(buckets(idx)) = i
      i += 1
    }

    I(0) = data.length

    i = 0
    while (i < data.length) {
      V(i) = buckets(data(i) & 0xFF)
      i += 1
    }

    V(data.length) = 0

    i = 1
    while (i < 256) {
      if (buckets(i) == buckets(i - 1) + 1) {
        I(buckets(i)) = -1
      }
      i += 1
    }

    I(0) = -1

    var h = 1
    var len = 0
    while (I(0) != -(data.length + 1)) {
      len = 0
      i = 0
      while (i < data.length + 1) {
        if (I(i) < 0) {
          len -= I(i)
          i -= I(i)
        } else {
          if (len != 0) {
            I(i - len) = -len
          }

          len = V(I(i)) + 1 - i
          split(I, V, i, len, h)
          i += len
          len = 0
        }
      }
      if (len != 0) {
        I(i - len) = -len
      }
      h += h
    }

    i = 0
    while (i < data.length + 1) {
      I(V(i)) = i
      i += 1
    }
  }

  def split(I: Array[Int], V: Array[Int], start: Int, len: Int, h: Int): Unit = {
    var i, j, k, x, tmp, jj, kk = 0

    if (len < 16) {
      k = start
      while (k < start + len) {
        j = 1
        x = V(I(k) + h)

        i = 1
        while (k + i < start + len) {
          if (V(I(k + i) + h) < x) {
            x = V(I(k + i) + h)
            j = 0
          }
          if (V(I(k + i) + h) == x) {
            tmp = I(k + j)
            I(k + j) = I(k + i)
            I(k + i) = tmp
            j += 1
          }
          i += 1
        }
        i = 0
        while (i < j) {
          V(I(k + i)) = k + j - 1
          i += 1
        }
        if (j == 1) {
          I(k) = -1
        }
        k += j
      }
      return
    }

    x = V(I(start + len / 2) + h)
    jj = 0
    kk = 0
    i = start
    while (i < start + len) {
      if (V(I(i) + h) < x) {
        jj += 1
      }

      if (V(I(i) + h) == x) {
        kk += 1
      }
      i += 1
    }
    jj += start
    kk += jj

    i = start
    j = 0
    k = 0
    while (i < jj) {
      if (V(I(i) + h) < x) {
        i += 1
      } else if (V(I(i) + h) == x) {
        tmp = I(i)
        I(i) = I(jj + j)
        I(jj + j) = tmp
        j += 1
      } else {
        tmp = I(i)
        I(i) = I(kk + k)
        I(kk + k) = tmp
        k += 1
      }
    }

    while (jj + j < kk) {
      if (V(I(jj + j) + h) == x) {
        j += 1
      } else {
        tmp = I(jj + j)
        I(jj + j) = I(kk + k)
        I(kk + k) = tmp
        k += 1
      }
    }

    if (jj > start) {
      split(I, V, start, jj - start, h)
    }

    i = 0
    while (i < kk - jj) {
      V(I(jj + i)) = kk - 1
      i += 1
    }

    if (jj == kk - 1) {
      I(jj) = -1
    }

    if (start + len > kk) {
      split(I, V, kk, start + len - kk, h)
    }
  }

  def search(I: Array[Int], oldBytes: Array[Byte], oldOffset: Int, newBytes: Array[Byte], newOffset: Int,
             start: Int, end: Int): SearchResult = {

    if (end - start < 2) {
      val x = matchlen(oldBytes, I(start), newBytes, newOffset)
      val y = matchlen(oldBytes, I(end), newBytes, newOffset)
      if (x > y) SearchResult(x, I(start)) else SearchResult(y, I(end))
    } else {
      val center = start + (end - start) / 2
      if (compareBytes(oldBytes, I(center), newBytes, newOffset) < 0) {
        search(I, oldBytes, 0, newBytes, newOffset, center, end)
      } else {
        search(I, oldBytes, 0, newBytes, newOffset, start, center)
      }
    }
  }

  private def matchlen(bytesA: Array[Byte], offsetA: Int, bytesB: Array[Byte], offsetB: Int): Int = {

    val oldLimit = bytesA.length - offsetA
    val newLimit = bytesB.length - offsetB

    var i = 0
    while (i < oldLimit && i < newLimit && (bytesA(i + offsetA) == bytesB(i + offsetB))) {
      i += 1
    }
    i
  }

  private def compareBytes(bytesA: Array[Byte], offsetA: Int,
                           bytesB: Array[Byte], offsetB: Int): Int = {
    val length = Math.min(bytesA.length - offsetA, bytesB.length - offsetB)
    var i, valA, valB = 0
    while (i < length && valA == valB) {
      valA = bytesA(i + offsetA) & 0xFF
      valB = bytesB(i + offsetB) & 0xFF
      i += 1
    }
    valA - valB
  }

}