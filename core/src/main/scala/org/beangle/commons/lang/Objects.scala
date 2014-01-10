/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang

import scala.collection.mutable.ListBuffer

object Objects {

  def default[T](clazz: Class[T]): T = Primitives.default(clazz)
  /**
   * <p>
   * Compares two objects for equality, where either one or both objects may be {@code null}.
   * </p>
   *
   * <pre>
   * equals(null, null)                  = true
   * equals(null, "")                    = false
   * equals("", null)                    = false
   * equals("", "")                      = true
   * equals(Boolean.TRUE, null)          = false
   * equals(Boolean.TRUE, "true")        = false
   * equals(Boolean.TRUE, Boolean.TRUE)  = true
   * equals(Boolean.TRUE, Boolean.FALSE) = false
   * </pre>
   *
   * @param a the first object, may be {@code null}
   * @param b the second object, may be {@code null}
   * @return {@code true} if the values of both objects are the same
   * @since 3.0
   */
  def equals(a: Any, b: Any): Boolean = (a == b)

  /**
   * <p>
   * Compares two object array for equality, where either one or both objects may be {@code null}.
   * </p>
   */
  def equals(a: Array[Any], b: Array[Any]): Boolean = {
    if (a eq b) return true
    if ((null == a) || (null == b)) return false
    if (a.length != b.length) return false
    for (i <- 0 until a.length if Objects.!=(a(i), b(i))) return false
    true
  }

  /**
   * <p>
   * Gets the {@code toString} of an {@code Object} returning an empty string ("") if {@code null}
   * input.
   * </p>
   *
   * <pre>
   * toString(null)         = ""
   * toString("")           = ""
   * toString("bat")        = "bat"
   * toString(Boolean.TRUE) = "true"
   * </pre>
   *
   * @see String#valueOf(Object)
   * @param obj the Object to {@code toString}, may be null
   * @return the passed in Object's toString, or nullStr if {@code null} input
   * @since 3.0
   */
  def toString(obj: AnyRef): String = if (null == obj) "" else obj.toString

  /**
   * <p>
   * Returns a default value if the object passed is {@code null}.
   * </p>
   *
   * <pre>
   * defaultIfNull(null, null)      = null
   * defaultIfNull(null, "")        = ""
   * defaultIfNull(null, "zz")      = "zz"
   * defaultIfNull("abc", *)        = "abc"
   * defaultIfNull(Boolean.TRUE, *) = Boolean.TRUE
   * </pre>
   *
   * @param <T> the type of the object
   * @param object the {@code Object} to test, may be {@code null}
   * @param defaultValue the default value to return, may be {@code null}
   * @return {@code object} if it is not {@code null}, defaultValue otherwise
   * @since 3.0
   */
  def defaultIfNull[T](value: T, defaultValue: T): T = {
    if (value != null) value else defaultValue
  }

  /**
   * Return a hex String form of an object's identity hash code.
   *
   * @param obj the object
   * @return the object's identity code in hex notation
   */
  def getIdentityHexString(obj: AnyRef): String = {
    Integer.toHexString(System.identityHashCode(obj))
  }

  def equalsBuilder(): EqualsBuilder = new EqualsBuilder()

  /**
   * Creates an instance of {@link ToStringBuilder}.
   * <p>
   * This is helpful for implementing {@link Object#toString()}. Specification by example:
   *
   * <pre>
   * {@code
   *   // Returns "ClassName{}"
   *   Objects.toStringBuilder(this)
   *       .toString();
   *
   *   // Returns "ClassName{x=1}"
   *   Objects.toStringBuilder(this)
   *       .add("x", 1)
   *       .toString();
   *
   *   // Returns "MyObject{x=1}"
   *   Objects.toStringBuilder("MyObject")
   *       .add("x", 1)
   *       .toString();
   *
   *   // Returns "ClassName{x=1, y=foo}"
   *   Objects.toStringBuilder(this)
   *       .add("x", 1)
   *       .add("y", "foo")
   *       .toString();
   *   }}
   *
   *   // Returns "ClassName{x=1}"
   *   Objects.toStringBuilder(this)
   *       .omitNullValues()
   *       .add("x", 1)
   *       .add("y", null)
   *       .toString();
   *   }}
   * </pre>
   *
   * @param self the object to generate the string for (typically {@code this}),
   *          used only for its class name
   * @since 3.1
   */
  def toStringBuilder(self: AnyRef): ToStringBuilder = {
    new ToStringBuilder(simpleName(self.getClass))
  }

  /**
   * Creates an instance of {@link ToStringBuilder} in the same manner as
   * {@link Objects#toStringBuilder(Object)}, but using the name of {@code clazz} instead of using
   * an
   * instance's {@link Object#getClass()}.
   * <p>
   *
   * @param clazz the {@link Class} of the instance
   */
  def toStringBuilder(clazz: Class[_]): ToStringBuilder = new ToStringBuilder(simpleName(clazz))

  /**
   * Creates an instance of {@link ToStringBuilder} in the same manner as
   * {@link Objects#toStringBuilder(Object)}, but using {@code className} instead
   * of using an instance's {@link Object#getClass()}.
   *
   * @param className the name of the instance type
   */
  def toStringBuilder(className: String): ToStringBuilder = new ToStringBuilder(className)

  /**
   * More readable than {@link Class#getSimpleName()}
   */
  private def simpleName(clazz: Class[_]): String = {
    var name = clazz.getName
    // the nth anonymous class has a class name ending in "Outer$n"
    // and local inner classes have names ending in "Outer.$1Inner"
    name = name.replaceAll("\\$[0-9]+", "\\$")
    // we want the name of the inner class all by its lonesome
    var start = name.lastIndexOf('$')
    // if this isn't an inner class, just find the start of the
    // top level class name.
    if (start == -1) start = name.lastIndexOf('.')
    name.substring(start + 1)
  }

  /**
   * Support class for {@link Objects#toStringBuilder}.
   */
  class ToStringBuilder(val className: String) {

    private val values = new ListBuffer[ValueHolder]()

    private var omitnull: Boolean = false

    /**
     * When called, the formatted output returned by {@link #toString()} will
     * ignore {@code null} values.
     */
    def omitNull(): this.type = {
      omitnull = true
      this
    }

    /**
     * Adds a name/value pair to the formatted output in {@code name=value} format. If {@code value}
     * is {@code null}, the string {@code "null"} is used, unless {@link #omitNull()} is
     * called, in which case this
     * name/value pair will not be added.
     */
    def add(name: String, value: AnyRef): ToStringBuilder = {
      values += new ValueHolder(name + "=" + value, null != value)
      this
    }

    /**
     * Returns a string in the format specified by {@link Objects#toStringBuilder(Object)}.
     */
    override def toString(): String = {
      val builder = new StringBuilder(32).append(className).append('{')
      var needsSeparator = false
      for (valueHolder <- values if !omitnull || !valueHolder.isNull) {
        if (needsSeparator) builder.append(", ") else needsSeparator = true
        builder.append(valueHolder.value)
      }
      builder.append('}').toString
    }

    private class ValueHolder(val value: String, val isNull: Boolean) {
    }

  }

  /**
   * Equals Builder
   *
   * @author chaostone
   * @since 3.1.0
   */
  class EqualsBuilder {

    var rs: Boolean = true

    def add(lhs: AnyRef, rhs: AnyRef): EqualsBuilder = {
      if (!rs) return this
      rs &= Objects.equals(lhs, rhs)
      this
    }

    def add(lhs: Array[Any], rhs: Array[Any]): EqualsBuilder = {
      if (!rs) return this
      rs &= Objects.==(lhs, rhs)
      this
    }

    def add(lhs: Int, rhs: Int): EqualsBuilder = {
      if (!rs) return this
      rs &= (lhs == rhs)
      this
    }

    def add(lhs: Long, rhs: Long): EqualsBuilder = {
      if (!rs) return this
      rs &= (lhs == rhs)
      this
    }

    def add(lhs: Short, rhs: Short): EqualsBuilder = {
      if (!rs) return this
      rs &= (lhs == rhs)
      this
    }

    def add(lhs: Boolean, rhs: Boolean): EqualsBuilder = {
      if (!rs) return this
      rs &= (lhs == rhs)
      this
    }
    def isEquals: Boolean = rs;
  }
}
