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

package org.beangle.commons.bean.meta

import org.beangle.commons.bean.meta.MetaModel.ClassMeta

import java.io.{BufferedInputStream, BufferedOutputStream, DataInputStream, DataOutputStream, IOException, InputStream, OutputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.collection.mutable

/** Multi-class container: a metamodel.idx file holding many self-contained
  * [[MetaModel.ClassMeta]] blobs plus a directory mapping JVM internal class names
  * to blob offset/length, so a single class can be located without scanning.
  *
  * Format (big-endian, lengths u32):
  * {{{
  * header:    magic "BMXI" + version u16
  * directory: count u32, count × { nameLen u16 + name UTF-8 + offset u32 + length u32 }
  * blobs:     count × { MetaCodec.encode(cm) }   // directory order == blob order
  * }}}
  * Each blob is a self-contained v1 blob (own header + pool), so this is a thin
  * wrapper — register `.*metamodel\.idx` as one resource for native-image.
  */
object MetaIndex {

  private val Magic = "BMXI"
  private val Version = 1
  private val HeaderSize = 4 + 2 + 4 // magic + version + count

  /** Writes multiple ClassMetas into a metamodel.idx file (directory sorted by class name). */
  def write(file: Path, metas: Iterable[ClassMeta]): Unit = {
    val out = new BufferedOutputStream(Files.newOutputStream(file))
    try write(out, metas) finally out.close()
  }

  /** Writes multiple ClassMetas into a metamodel.idx stream. */
  def write(out: OutputStream, metas: Iterable[ClassMeta]): Unit = {
    val blobs = metas.toSeq.sortBy(_.clazz.getName).map(cm => cm -> MetaCodec.encode(cm))
    val nameBytes = blobs.map(_._1.clazz.getName.replace('.', '/').getBytes(StandardCharsets.UTF_8))
    val dirSize = nameBytes.map(nb => 10 + nb.length).sum
    val d = new DataOutputStream(out)
    d.writeBytes(Magic)
    d.writeShort(Version)
    d.writeInt(blobs.size)
    var offset = HeaderSize + dirSize
    nameBytes.indices foreach { i =>
      val nb = nameBytes(i)
      d.writeShort(nb.length); d.write(nb)
      d.writeInt(offset); d.writeInt(blobs(i)._2.length)
      offset += blobs(i)._2.length
    }
    blobs foreach { case (_, blob) => out.write(blob) }
    d.flush()
  }

  /** Reads all ClassMetas from a metamodel.idx file. */
  def read(file: Path): Seq[ClassMeta] = read(file, mutable.Set.empty)

  /** Reads all ClassMetas from a metamodel.idx file with string deduplication pool. */
  def read(file: Path, pool: mutable.Set[String]): Seq[ClassMeta] = {
    val in = new BufferedInputStream(Files.newInputStream(file))
    try read(in, pool) finally in.close()
  }

  /** Reads all ClassMetas from a metamodel.idx stream. */
  def read(in: InputStream): Seq[ClassMeta] = read(in, mutable.Set.empty)

  /** Reads all ClassMetas from a metamodel.idx stream with string deduplication pool.
    *
    * @param pool string deduplication set shared across blobs within this idx file.
    */
  def read(in: InputStream, pool: mutable.Set[String]): Seq[ClassMeta] = {
    val d = new DataInputStream(in)
    val count = readHeader(d)
    val entries = readDirectory(d, count)
    entries.map { case (_, _, len) =>
      val blob = new Array[Byte](len)
      d.readFully(blob)
      MetaCodec.parse(blob, pool)
    }
  }

  /** Finds a single ClassMeta by JVM internal class name (e.g. "org/example/User"). */
  def find(file: Path, className: String): Option[ClassMeta] = find(file, className, mutable.Set.empty)

  /** Finds a single ClassMeta by JVM internal class name with string deduplication pool. */
  def find(file: Path, className: String, pool: mutable.Set[String]): Option[ClassMeta] = {
    val in = new BufferedInputStream(Files.newInputStream(file))
    try find(in, className, pool) finally in.close()
  }

  /** Finds a single ClassMeta by JVM internal class name from a metamodel.idx stream. */
  def find(in: InputStream, className: String): Option[ClassMeta] = find(in, className, mutable.Set.empty)

  /** Finds a single ClassMeta by JVM internal class name from a metamodel.idx stream with string deduplication pool. */
  def find(in: InputStream, className: String, pool: mutable.Set[String]): Option[ClassMeta] = {
    val d = new DataInputStream(in)
    val count = readHeader(d)
    val entries = readDirectory(d, count)
    var position = HeaderSize + directorySize(entries)
    entries.collectFirst { case (name, off, len) if name == className =>
      skipFully(in, off - position)
      val blob = new Array[Byte](len)
      d.readFully(blob)
      MetaCodec.parse(blob, pool)
    }
  }

  private def readHeader(d: DataInputStream): Int = {
    val magic = new Array[Byte](4)
    d.readFully(magic)
    if (!new String(magic, StandardCharsets.US_ASCII).equals(Magic))
      throw new IllegalArgumentException("Not a metamodel index")
    val version = d.readUnsignedShort()
    if version != Version then throw new IllegalArgumentException(s"Unsupported metamodel index version $version, expected $Version")
    d.readInt()
  }

  private def readDirectory(d: DataInputStream, count: Int): Seq[(String, Int, Int)] = {
    (0 until count).map { _ =>
      val len = d.readUnsignedShort()
      val nb = new Array[Byte](len)
      d.readFully(nb)
      val name = new String(nb, StandardCharsets.UTF_8)
      val off = d.readInt(); val length = d.readInt()
      (name, off, length)
    }
  }

  private def directorySize(entries: Seq[(String, Int, Int)]): Int =
    entries.map(e => 10 + e._1.getBytes(StandardCharsets.UTF_8).length).sum

  private def skipFully(in: InputStream, n: Long): Unit = {
    var remaining = n
    while remaining > 0 do
      val skipped = in.skip(remaining)
      if skipped <= 0 then throw new IOException(s"Cannot skip $remaining bytes in metamodel index")
      remaining -= skipped
  }
}
