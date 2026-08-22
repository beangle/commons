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
import org.beangle.commons.io.Resources

import java.io.{BufferedInputStream, DataInputStream, InputStream}
import java.net.URL
import java.nio.charset.StandardCharsets
import scala.collection.mutable

/** Global registry for locating [[ClassMeta]] by class name or Class instance.
  *
  * Scans `classpath*:META-INF/beangle/metamodel.idx` at startup, builds an
  * in-memory index mapping class names to their idx file locations and blob
  * offsets, then resolves ClassMeta on demand without caching.
  *
  * {{{
  * // Lookup by Class
  * MetaModels.get(classOf[User]) match {
  *   case Some(cm) => println(cm.properties)
  *   case None     => println("not found")
  * }
  *
  * // Lookup by class name (JVM internal or dot-separated)
  * MetaModels.get("org.example.User")
  * }}}
  */
object MetaModels {

  /** Index entry: URL of the idx file + blob offset + blob length. */
  private case class Entry(url: URL, offset: Int, length: Int)

  /** Class name -> Entry index (lazy initialized). */
  private lazy val index: Map[String, Entry] = buildIndex()

  /** Returns ClassMeta for the given class, or None if not found. */
  def get(clazz: Class[_]): Option[ClassMeta] = get(clazz.getName)

  /** Returns ClassMeta for the given class name (dot-separated or JVM internal), or None. */
  def get(className: String): Option[ClassMeta] = {
    val normalized = normalize(className)
    index.get(normalized).map(load)
  }

  /** Returns true if ClassMeta is available for the given class. */
  def contains(clazz: Class[_]): Boolean = contains(clazz.getName)

  /** Returns true if ClassMeta is available for the given class name. */
  def contains(className: String): Boolean = index.contains(normalize(className))

  /** Returns all registered class names. */
  def classNames: Set[String] = index.keySet.toSet

  /** Builds the index by scanning all metamodel.idx files on the classpath. */
  private def buildIndex(): Map[String, Entry] = {
    val map = mutable.HashMap.empty[String, Entry]
    val urls = Resources.load("classpath*:META-INF/beangle/metamodel.idx")
    urls.foreach { url =>
      try {
        val entries = readDirectory(url)
        entries.foreach { case (name, offset, length) =>
          map.put(name, Entry(url, offset, length))
        }
      } catch {
        case _: Exception => // skip malformed idx files
      }
    }
    map.toMap
  }

  /** Reads the directory from a metamodel.idx URL, returning (className, offset, length) tuples. */
  private def readDirectory(url: URL): Seq[(String, Int, Int)] = {
    val in = new DataInputStream(new BufferedInputStream(url.openStream()))
    try {
      val magic = new Array[Byte](4)
      in.readFully(magic)
      if (!new String(magic, StandardCharsets.US_ASCII).equals("BNIX"))
        throw new IllegalArgumentException("Not a beaninfo index")
      val version = in.readUnsignedShort()
      if (version != 1)
        throw new IllegalArgumentException(s"Unsupported beaninfo index version $version")
      val count = in.readInt()
      (0 until count).map { _ =>
        val len = in.readUnsignedShort()
        val nb = new Array[Byte](len)
        in.readFully(nb)
        val name = new String(nb, StandardCharsets.UTF_8)
        val offset = in.readInt()
        val length = in.readInt()
        (name, offset, length)
      }
    } finally {
      in.close()
    }
  }

  /** Loads a single ClassMeta from the idx file at the given entry. */
  private def load(entry: Entry): ClassMeta = {
    val in = new DataInputStream(new BufferedInputStream(entry.url.openStream()))
    try {
      // Skip header + directory to reach blobs
      skipToOffset(in, entry.offset)
      val blob = new Array[Byte](entry.length)
      in.readFully(blob)
      MetaCodec.parse(blob)
    } finally {
      in.close()
    }
  }

  /** Skips bytes in the stream to reach the specified offset. */
  private def skipToOffset(in: InputStream, targetOffset: Int): Unit = {
    var remaining = targetOffset.toLong
    while (remaining > 0) {
      val skipped = in.skip(remaining)
      if (skipped <= 0) throw new IOException(s"Cannot skip to offset $targetOffset")
      remaining -= skipped
    }
  }

  /** Normalizes a class name to JVM internal format (dot-separated -> slash-separated). */
  private def normalize(className: String): String = {
    className.replace('.', '/')
  }

  private class IOException(message: String) extends Exception(message)
}
