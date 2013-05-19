/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.meta

/**
 * Unique Key
 *
 * @author chaostone
 */
class UniqueKey(name: String) extends Constraint(name) {

  override def clone: this.type = return super.clone();
}
