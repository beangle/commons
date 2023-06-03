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

package org.beangle.commons.file.diff.bsdiff

object Format {

  val Compression = "bzip2";

  //Size of the Header, in bytes.  4 fields * 8 bytes = 32 bytes
  val HeaderLength = 32;

  // Magic number to mark the start of a bsdiff header.
  val HeaderMagic = "BSDIFF40";

  /** Data structure that encapsulates a bsdiff header.  The header is composed of
    * 8-byte fields, starting with the magic number "BSDIFF40."
    * <p/>
    * 0: BSDIFF40
    * 8: length of control block
    * 16: length of the diff block
    * 24: size of the output file
    */
  case class Header(controlLength: Int, diffLength: Int, outputLength: Int) {
    require(controlLength > 0 && diffLength > 0 && outputLength > 0)
  }

  case class Block(diffLength: Int, extraLength: Int, seekLength: Int)
}
