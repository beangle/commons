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

import scala.collection.mutable.ListBuffer

object Objects {

  /** Returns the default value for the given primitive/wrapper class.
   *
   * @param clazz the class (e.g. Int, Long, Boolean)
   * @return the default value
   */
  @inline
  def default[T](clazz: Class[T]): T = Primitives.default(clazz)

  /** Compares two objects for equality, where either one or both objects may be `null`.
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
   * @param a the first object, may be `null`
   * @param b the second object, may be `null`
   * @return `true` if the values of both objects are the same
   * @since 3.0
   */
  @inline
  def equals(a: Any, b: Any): Boolean = {
    a == b
  }

  /** Compares two object arrays for equality.
   *
   * @param a the first array
   * @param b the second array
   * @return true if both null or elements equal
   */
  def equals(a: Array[Any], b: Array[Any]): Boolean = {
    if a eq b then true
    else if (null == a) || (null == b) then false
    else if a.length != b.length then false
    else !Range(0, a.length).exists(i => a(i) != b(i))
  }

  /** Gets the `toString` of an `Object` returning an empty string ("") if `null`
   * input.
   *
   * <pre>
   * toString(null)         = ""
   * toString("")           = ""
   * toString("bat")        = "bat"
   * toString(Boolean.TRUE) = "true"
   * </pre>
   *
   * @see String#valueOf(Object)
   * @param obj the Object to `toString`, may be null
   * @return the passed in Object's toString, or nullStr if `null` input
   * @since 3.0
   */
  def toString(obj: AnyRef): String = if (null == obj) "" else obj.toString

  /** Returns the given value if non-null, otherwise the default value.
   * {{{
   * nvl(null, null)      = null
   * nvl(null, "")        = ""
   * nvl(null, "zz")      = "zz"
   * nvl("abc", *)        = "abc"
   * nvl(Boolean.TRUE, *) = Boolean.TRUE
   * }}}
   *
   * @param s       the value to check
   * @param default the default value when s is null
   * @return s if non-null, otherwise default
   */
  def nvl[T](s: T, default: => T): T = if null == s then default else s

  /** Return a hex String form of an object's identity hash code.
   *
   * @param obj the object
   * @return the object's identity code in hex notation
   */
  def getIdentityHexString(obj: AnyRef): String = Integer.toHexString(System.identityHashCode(obj))

  /** Creates an EqualsBuilder for stepwise equality comparison. */
  def equalsBuilder: EqualsBuilder = new EqualsBuilder()

  /** Creates a CompareBuilder for stepwise ordering comparison. */
  def compareBuilder: CompareBuilder = new CompareBuilder()

  /** Creates an instance of `ToStringBuilder`.
   * <p>
   * This is helpful for implementing `Object#toString()`. Specification by example:
   *
   * {{{
   *   // Returns "ClassName{}"
   *   Objects.toStringBuilder(this).toString();
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
   * }}}
   *
   * @param self the object to generate the string for (typically `this`),
   *             used only for its class name
   * @since 3.1
   */
  def toStringBuilder(self: AnyRef): ToStringBuilder = new ToStringBuilder(simpleName(self.getClass))

  /** Creates an instance of [[ToStringBuilder]] in the same manner as
   * `toStringBuilder(AnyRef)`, but using the name of `clazz` instead of using
   * an instance's `Object#getClass()`.
   * <p>
   *
   * @param clazz the Class of the instance
   */
  def toStringBuilder(clazz: Class[_]): ToStringBuilder = new ToStringBuilder(simpleName(clazz))

  /** Creates an instance of [[ToStringBuilder]] in the same manner as
   * `toStringBuilder(AnyRef)`, but using `className` instead of using an class instance.
   *
   * @param className the name of the instance type
   */
  def toStringBuilder(className: String): ToStringBuilder = new ToStringBuilder(className)

  /** More readable than `Class#getSimpleName`
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

  /** Support class for Objects.toStringBuilder.
   */
  class ToStringBuilder(val className: String) {

    private val values = new ListBuffer[ValueHolder]()

    private var omitnull: Boolean = false

    /**
     * When called, the formatted output returned by `toString` will ignore `null` values.
     */
    def omitNull(): this.type = {
      omitnull = true
      this
    }

    /**
     * Adds a name/value pair to the formatted output in `name=value` format. If `value`
     * is `null`, the string `null` is used, unless `Objects#omitNull()` is
     * called, in which case this
     * name/value pair will not be added.
     */
    def add(name: String, value: Any): ToStringBuilder = {
      values += new ValueHolder(name + "=" + value, null != value)
      this
    }

    /** Returns a string in the format specified by `Objects#toStringBuilder(Object)`. */
    override def toString: String = {
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

  /** Equals Builder
   *
   * @author chaostone
   * @since 3.1.0
   */
  class EqualsBuilder {

    var rs: Boolean = true

    /** Adds pair to comparison; rs becomes false if unequal. */
    def add(lhs: Any, rhs: Any): EqualsBuilder = {
      if (!rs) return this
      if (lhs.getClass.isArray && rhs.getClass.isArray)
        rs &= Objects.equals(lhs.asInstanceOf[Array[Any]], rhs.asInstanceOf[Array[Any]])
      else
        rs &= Objects.equals(lhs, rhs)
      this
    }

    /** Adds int pair to comparison. */
    def add(lhs: Int, rhs: Int): EqualsBuilder = {
      if (!rs) return this
      rs &= (lhs == rhs)
      this
    }

    /** Adds long pair to comparison. */
    def add(lhs: Long, rhs: Long): EqualsBuilder = {
      if (!rs) return this
      rs &= (lhs == rhs)
      this
    }

    /** Adds short pair to comparison. */
    def add(lhs: Short, rhs: Short): EqualsBuilder = {
      if (!rs) return this
      rs &= (lhs == rhs)
      this
    }

    /** Adds boolean pair to comparison. */
    def add(lhs: Boolean, rhs: Boolean): EqualsBuilder = {
      if (!rs) return this
      rs &= (lhs == rhs)
      this
    }

    /** Returns true if all added pairs are equal. */
    def isEquals: Boolean = rs
  }

  /** Compare Builder
   *
   * @since 4.2.4
   */
  class CompareBuilder {
    private var comparison: Int = _

    /** Adds pair to comparison; stops when first difference found. */
    def add(lhs: Any, rhs: Any, ordering: Ordering[Any] = null): this.type = {
      if comparison != 0 then return this
      if lhs == rhs then return this
      if (lhs == null) {
        comparison = -1
        return this
      }
      if (rhs == null) {
        comparison = +1
        return this
      }
      if (lhs.getClass.isArray) {
        val lhsa = lhs.asInstanceOf[Array[_]]
        val rhsa = rhs.asInstanceOf[Array[_]]
        if (lhsa.length != rhsa.length) {
          comparison = if (lhsa.length < rhsa.length) -1 else +1
          return this
        }
        var i = 0
        while (i < lhsa.length && comparison == 0) {
          add(lhsa(i), rhsa(i), ordering)
          i += 1
        }
        return this
      }
      if (null == ordering) {
        comparison = lhs match {
          case lhso: Ordered[_] => lhso.asInstanceOf[Ordered[Any]].compare(rhs)
          case lhsc: Comparable[_] => lhsc.asInstanceOf[Comparable[Any]].compareTo(rhs)
        }
        this
      } else {
        comparison = ordering.compare(lhs, rhs)
        this
      }
    }

    /** Returns the comparison result (-1, 0, or 1). */
    def toComparison: Int = comparison

    /** Returns the comparison result. */
    def build(): Int = comparison
  }

}
