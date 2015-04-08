/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.commons.lang

import java.util.Iterator

/**
 * Assertion tool class
 *
 * @author chaostone
 * @since 3.0.0
 */
object Assert {

  private val NotEmptyCharSeqMsg = "The validated character sequence is empty"

  private val IsNullMsg = "The validated object is null"

  private val IsTrueMsg = "The validated expression is false"

  /**
   * <p>
   * Validate that the argument condition is {@code true}; otherwise throwing an exception. This
   * method is useful when validating according to an arbitrary boolean expression, such as
   * validating a primitive number or using your own custom validation expression.
   * </p>
   *
   * <pre>
   * Assert.isTrue(i &gt; 0);
   * Assert.isTrue(myObject.isOk());
   * </pre>
   * <p>
   * The message of the exception is &quot;The validated expression is false&quot;.
   * </p>
   *
   * @param expression the boolean expression to check
   * @throws IllegalArgumentException if expression is {@code false}
   * @see #isTrue(boolean, String, Object...)
   */
  def isTrue(expression: Boolean) {
    if (expression == false) throw new IllegalArgumentException(IsTrueMsg)
  }

  /**
   * <p>
   * Validate that the argument condition is {@code true}; otherwise throwing an exception with the
   * specified message. This method is useful when validating according to an arbitrary boolean
   * expression, such as validating a primitive number or using your own custom validation
   * expression.
   * </p>
   *
   * <pre>
   * Assert.isTrue(i &gt;= min &amp;&amp; i &lt;= max, &quot;The value must be between %d and %d&quot;, min, max);
   * Assert.isTrue(myObject.isOk(), &quot;The object is not okay&quot;);
   * </pre>
   *
   * @param expression the boolean expression to check
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *          null
   * @param values the optional values for the formatted exception message, null array not
   *          recommended
   * @throws IllegalArgumentException if expression is {@code false}
   * @see #isTrue(boolean)
   */
  def isTrue(expression: Boolean, message: String, values: AnyRef*) {
    if (!expression) throw new IllegalArgumentException(String.format(message, values))
  }

  /**
   * <p>
   * Validate that the specified argument is not {@code null}; otherwise throwing an exception.
   *
   * <pre>
   * Assert.notNull(myObject, &quot;The object must not be null&quot;);
   * </pre>
   * <p>
   * The message of the exception is &quot;The validated object is null&quot;.
   * </p>
   *
   * @param <T> the object type
   * @param value the object to check
   * @return the validated object (never {@code null} for method chaining)
   * @throws NullPointerException if the object is {@code null}
   * @see #notNull(Object, String, Object...)
   */
  def notNull[T](value: T): T = notNull(value, IsNullMsg)

  /**
   * <p>
   * Validate that the specified argument is not {@code null}; otherwise throwing an exception with
   * the specified message.
   *
   * <pre>
   * Assert.notNull(myObject, &quot;The object must not be null&quot;);
   * </pre>
   *
   * @param <T> the object type
   * @param object the object to check
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *          null
   * @param values the optional values for the formatted exception message
   * @return the validated object (never {@code null} for method chaining)
   * @throws NullPointerException if the object is {@code null}
   * @see #notNull(Object)
   */
  def notNull[T](value: T, message: String, values: AnyRef*): T = {
    if (value == null) throw new NullPointerException(String.format(message, values))
    value
  }

  /**
   * <p>
   * Validate that the specified argument array is neither {@code null} nor a length of zero (no
   * elements); otherwise throwing an exception.
   *
   * <pre>
   * Assert.notEmpty(myArray);
   * </pre>
   * <p>
   * The message in the exception is &quot;The validated array is empty&quot;.
   *
   * @param <T> the array type
   * @return the validated array (never {@code null} method for chaining)
   * @throws NullPointerException if the array is {@code null}
   * @throws IllegalArgumentException if the array is empty
   */
  def notEmpty[T <: CharSequence](chars: T): T = {
    if (chars == null) throw new NullPointerException(NotEmptyCharSeqMsg)
    if (chars.length == 0) throw new IllegalArgumentException(NotEmptyCharSeqMsg)
    chars
  }

  /**
   * <p>
   * Validate that the specified argument character sequence is neither {@code null} nor a length of
   * zero (no characters); otherwise throwing an exception with the specified message.
   *
   * <pre>
   * Assert.notEmpty(myString);
   * </pre>
   * <p>
   * The message in the exception is &quot;The validated character sequence is empty&quot;.
   * </p>
   *
   * @param <T> the character sequence type
   * @param chars the character sequence to check, validated not null by this method
   * @return the validated character sequence (never {@code null} method for chaining)
   * @throws NullPointerException if the character sequence is {@code null}
   * @throws IllegalArgumentException if the character sequence is empty
   */
  def notEmpty[T <: CharSequence](chars: T, message: String, values: AnyRef*): T = {
    if (chars == null) throw new NullPointerException(String.format(message, values))
    if (chars.length == 0) throw new IllegalArgumentException(String.format(message, values))
    chars
  }

  /**
   * <p>
   * Validate that the specified argument iterable is neither {@code null} nor contains any elements
   * that are {@code null}; otherwise throwing an exception with the specified message.
   *
   * <pre>
   * Assert.noNullElements(myCollection, &quot;The collection contains null at position %d&quot;);
   * </pre>
   * <p>
   * If the iterable is {@code null}, then the message in the exception is &quot;The validated
   * object is null&quot;.
   * </p>
   * <p>
   * If the iterable has a {@code null} element, then the iteration index of the invalid element is
   * appended to the {@code values} argument.
   * </p>
   *
   * @param <T> the iterable type
   * @param iterable the iterable to check, validated not null by this method
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not null
   * @param values the optional values for the formatted exception message, null array not recommended
   * @return the validated iterable (never {@code null} method for chaining)
   * @throws NullPointerException if the array is {@code null}
   * @throws IllegalArgumentException if an element is {@code null}
   */
  def noNullElements[T <: java.lang.Iterable[_]](iterable: T, message: String, values: AnyRef*): T = {
    notNull(iterable)
    var i = 0
    var it = iterable.iterator()
    while (it.hasNext) {
      if (it.next() == null) throw new IllegalArgumentException(String.format(message, String.valueOf(i)))
      i += 1
    }
    iterable
  }
}
