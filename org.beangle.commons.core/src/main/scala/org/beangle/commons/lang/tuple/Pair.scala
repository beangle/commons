/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.lang.tuple

import java.io.Serializable
import java.util.Map
import org.beangle.commons.lang.Objects

object Pair {

  /**
   * <p>
   * Obtains an immutable pair of from two objects inferring the generic types.
   * </p>
   * <p>
   * This factory allows the pair to be created using inference to obtain the generic types.
   * </p>
   *
   * @param <L> the left element type
   * @param <R> the right element type
   * @param left the left element, may be null
   * @param right the right element, may be null
   * @return a pair formed from the two parameters, not null
   */
  def of[L, R](left: L, right: R): Pair[L, R] = new Pair[L, R](left, right)
}

/**
 * <p>
 * A immutable pair consisting of two elements.
 * </p>
 *
 * @author chaostone
 * @param <L> the left element type
 * @param <R> the right element type
 */
@SerialVersionUID(-7643900124010501814L)
class Pair[L, R](val left: L, val right: R) extends Map.Entry[L, R]() with Serializable {

  def getLeft(): L = left

  def getRight(): R = right

  /**
   * <p>
   * Throws {@code UnsupportedOperationException}.
   * </p>
   * <p>
   * This pair is immutable, so this operation is not supported.
   * </p>
   *
   * @param value the value to set
   * @return never
   * @throws UnsupportedOperationException as this operation is not supported
   */
  def setValue(value: R): R = {
    throw new UnsupportedOperationException()
  }

  /**
   * <p>
   * Gets the key from this pair.
   * </p>
   * <p>
   * This method implements the {@code Map.Entry} interface returning the left element as the key.
   * </p>
   *
   * @return the left element as the key, may be null
   */
  def getKey(): L = left

  /**
   * <p>
   * Gets the value from this pair.
   * </p>
   * <p>
   * This method implements the {@code Map.Entry} interface returning the right element as the
   * value.
   * </p>
   *
   * @return the right element as the value, may be null
   */
  def getValue(): R = right

  /**
   * <p>
   * Compares this pair to another based on the two elements.
   * </p>
   *
   * @param obj the object to compare to, null returns false
   * @return true if the elements of the pair are equal
   */
  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[Map.Entry[_, _]]) {
      val other = obj.asInstanceOf[Map.Entry[_, _]]
      return getKey == other.getKey && getValue == other.getValue
    }
    false
  }

  /**
   * <p>
   * Returns a suitable hash code. The hash code follows the definition in {@code Map.Entry}.
   * </p>
   *
   * @return the hash code
   */
  override def hashCode(): Int = {
    (if (getKey == null) 0 else getKey.hashCode) ^ (if (getValue == null) 0 else getValue.hashCode)
  }

  override def toString(): String = {
    new StringBuilder().append('(').append(getLeft).append(',')
      .append(getRight).append(')').toString
  }
}