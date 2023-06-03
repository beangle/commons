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

import java.io.{DataInputStream, IOException, InputStream, OutputStream}

/** bsdiff encodes offsets (represented by the C off_t type) as 64-bit chunks.
  * In this implementation only 32-bit signed integers are supported, but the
  * additional encoding steps are left to illustrate the process (which, in Java,
  * would encode/decode a long primitive data type).
  */
object Offset {
  /** Size of a bsdiff-encoded offset, in bytes.
    */
  val OFFSET_SIZE = 8

  /** Reads a bsdiff-encoded offset (based on the C off_t type) from an
    * {@link InputStream}.
    */
  def readOffset(in: InputStream): Int = {
    val buf = new Array[Byte](OFFSET_SIZE)
    val bytesRead = in.read(buf)
    if (bytesRead < OFFSET_SIZE)
      throw new IOException("Could not read offset.")

    var y = 0
    y = buf(7) & 0x7F
    y *= 256
    y += buf(6) & 0xFF
    y *= 256
    y += buf(5) & 0xFF
    y *= 256
    y += buf(4) & 0xFF
    y *= 256
    y += buf(3) & 0xFF
    y *= 256
    y += buf(2) & 0xFF
    y *= 256
    y += buf(1) & 0xFF
    y *= 256
    y += buf(0) & 0xFF

    /* An integer overflow occurred */
    if (y < 0)
      throw new IOException(
        "Integer overflow: 64-bit offsets not supported.")

    if ((buf(7) & 0x80) != 0)
      y = -y

    return y
  }

  /** Writes a bsdiff-encoded offset to an {@link OutputStream}.
    */
  def writeOffset(value: Int, out: OutputStream): Unit = {
    val buf = new Array[Byte](OFFSET_SIZE)
    var y = 0

    if (value < 0) {
      y = -value
      /* Set the sign bit */
      buf(7) = (buf(7) | 0x80).asInstanceOf[Byte]
    } else
      y = value

    buf(0) = (buf(0) | y % 256).asInstanceOf[Byte]
    y -= buf(0) & 0xFF
    y /= 256
    buf(1) = (buf(1) | y % 256).asInstanceOf[Byte]
    y -= buf(1) & 0xFF
    y /= 256
    buf(2) = (buf(2) | y % 256).asInstanceOf[Byte]
    y -= buf(2) & 0xFF
    y /= 256
    buf(3) = (buf(3) | y % 256).asInstanceOf[Byte]
    y -= buf(3) & 0xFF
    y /= 256
    buf(4) = (buf(4) | y % 256).asInstanceOf[Byte]
    y -= buf(4) & 0xFF
    y /= 256
    buf(5) = (buf(5) | y % 256).asInstanceOf[Byte]
    y -= buf(5) & 0xFF
    y /= 256
    buf(6) = (buf(6) | y % 256).asInstanceOf[Byte]
    y -= buf(6) & 0xFF
    y /= 256
    buf(7) = (buf(7) | y % 256).asInstanceOf[Byte]

    out.write(buf)
  }

  def writeBlock(b: Format.Block, out: OutputStream): Unit = {
    writeOffset(b.diffLength, out)
    writeOffset(b.extraLength, out)
    writeOffset(b.seekLength, out)
  }

  def readBlock(in: InputStream): Format.Block =
    Format.Block(readOffset(in), readOffset(in), readOffset(in))

  def writeHeader(h: Format.Header, out: OutputStream): Unit = {
    out.write(Format.HeaderMagic.getBytes())
    writeOffset(h.controlLength, out)
    writeOffset(h.diffLength, out)
    writeOffset(h.outputLength, out)
  }

  def readHeader(in: InputStream): Format.Header = {
    val headerIn = new DataInputStream(in)
    val buf = new Array[Byte](8)

    headerIn.read(buf)
    val magic = new String(buf)
    if (!"BSDIFF40".equals(magic))
      throw new RuntimeException("Header missing magic number")
    Format.Header(readOffset(headerIn), readOffset(headerIn), readOffset(headerIn))
  }
}
