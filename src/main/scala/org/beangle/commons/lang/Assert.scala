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

package org.beangle.commons.lang

/** Assertion utilities for validation.
 *
 * @author chaostone
 * @since 3.0.0
 */
object Assert {

  private val NotEmptyCharSeqMsg = "The validated character sequence is empty"

  private val IsNullMsg = "The validated object is null"

  private val IsTrueMsg = "The validated expression is false"

  /** Assert is True
   * <p>
   * Validate that the argument condition is `true`; otherwise throwing an exception. This
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
   * @throws IllegalArgumentException if expression is `false`
   * @see #isTrue(boolean, String, Object...)
   */
  def isTrue(expression: Boolean): Unit = {
    if (!expression) throw new IllegalArgumentException(IsTrueMsg)
  }

  /** Validate that the argument condition is `true`;
   * <p>
   * otherwise throwing an exception with the
   * specified message. This method is useful when validating according to an arbitrary boolean
   * expression, such as validating a primitive number or using your own custom validation
   * expression.
   * </p>
   *
   * {{{
   * Assert.isTrue(i &gt;= min &amp;&amp; i &lt;= max, &quot;The value must be between %d and %d&quot;, min, max);
   * Assert.isTrue(myObject.isOk(), &quot;The object is not okay&quot;);
   * }}}
   *
   * @param expression the boolean expression to check
   * @param message    the `String# format(String,Object...)` exception message if invalid, not null
   * @param values     the optional values for the formatted exception message, null array not
   *                   recommended
   * @throws IllegalArgumentException if expression is `false`
   * @see #isTrue(boolean)
   */
  def isTrue(expression: Boolean, message: String, values: AnyRef*): Unit =
    if (!expression) throw new IllegalArgumentException(String.format(message, values))

  /** Validate that the specified argument is not `null`; otherwise throwing an exception.
   *
   * <pre>
   * Assert.notNull(myObject, &quot;The object must not be null&quot;);
   * </pre>
   * The message of the exception is &quot;The validated object is null&quot;.
   * </p>
   *
   * @param value the object to check
   * @return the validated object (never `null` for method chaining)
   * @throws NullPointerException if the object is `null`
   * @see #notNull(Object, String, Object...)
   */
  def notNull[T](value: T): T = notNull(value, IsNullMsg)

  /** Validate that the specified argument is not `null`; otherwise throwing an exception with
   * the specified message.
   *
   * {{{Assert.notNull(myObject, &quot;The object must not be null&quot;)}}}
   *
   * @param value   the object to check
   * @param message the `String#format(String,Object...)`  exception message if invalid, not null
   * @param values  the optional values for the formatted exception message
   * @return the validated object (never `null` for method chaining)
   * @throws NullPointerException if the object is `null`
   * @see #notNull(Object)
   */
  def notNull[T](value: T, message: String, values: AnyRef*): T = {
    if (value == null) throw new NullPointerException(String.format(message, values))
    value
  }

  /** Validate that the specified argument array is neither `null` nor a length of zero (no
   * elements); otherwise throwing an exception.
   *
   * <pre>
   * Assert.notEmpty(myArray);
   * </pre>
   * The message in the exception is &quot;The validated array is empty&quot;.
   *
   * @return the validated array (never `null` method for chaining)
   * @throws NullPointerException     if the array is `null`
   * @throws IllegalArgumentException if the array is empty
   */
  def notEmpty[T <: CharSequence](chars: T): T = {
    if (chars == null) throw new NullPointerException(NotEmptyCharSeqMsg)
    if (chars.length == 0) throw new IllegalArgumentException(NotEmptyCharSeqMsg)
    chars
  }

  /** Validate that the specified argument character sequence is neither `null` nor a length of
   * zero (no characters); otherwise throwing an exception with the specified message.
   *
   * {{{Assert.notEmpty(myString)}}}
   * The message in the exception is &quot;The validated character sequence is empty&quot;.
   *
   * @param chars the character sequence to check, validated not null by this method
   * @return the validated character sequence (never `null` method for chaining)
   * @throws NullPointerException     if the character sequence is `null`
   * @throws IllegalArgumentException if the character sequence is empty
   */
  def notEmpty[T <: CharSequence](chars: T, message: String, values: AnyRef*): T = {
    if (chars == null) throw new NullPointerException(String.format(message, values))
    if (chars.length == 0) throw new IllegalArgumentException(String.format(message, values))
    chars
  }

  /** Validate that the specified argument iterable is neither `null` nor contains any elements
   * that are `null`; otherwise throwing an exception with the specified message.
   *
   * <pre>
   * Assert.noNullElements(myCollection, &quot;The collection contains null at position %d&quot;);
   * </pre>
   * If the iterable is `null`, then the message in the exception is &quot;The validated
   * object is null&quot;.
   * </p>
   * <p>
   * If the iterable has a `null` element, then the iteration index of the invalid element is
   * appended to the `values` argument.
   * </p>
   *
   * @param iterable the iterable to check, validated not null by this method
   * @param message  the `String#format(String,Object...)` exception message if invalid, not null
   * @param values   the optional values for the formatted exception message, null array not recommended
   * @return the validated iterable (never `null` method for chaining)
   * @throws NullPointerException     if the array is `null`
   * @throws IllegalArgumentException if an element is `null`
   */
  def noNullElements[T <: java.lang.Iterable[_]](iterable: T, message: String, values: AnyRef*): T = {
    notNull(iterable)
    var i = 0
    val it = iterable.iterator()
    while (it.hasNext) {
      if (it.next() == null) throw new IllegalArgumentException(String.format(message, String.valueOf(i)))
      i += 1
    }
    iterable
  }
}
