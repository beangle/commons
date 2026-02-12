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

package org.beangle.commons.io

import org.beangle.commons.lang.Charsets.UTF_8
import org.beangle.commons.lang.Strings.replace
import org.beangle.commons.lang.SystemInfo

import java.io.*
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file.Files as JFiles

object Files {

  private val copyBufferSize = 1024 * 1024 * 30
  private val reservedChars = Array('/', '\\', ':', '*', '?', '"', '>', '<', '|', '\t', '\n')

  /** File path separator for the current platform (e.g. / or \). */
  val / = File.separator

  private def fileName(name: String): String = {
    if (/ == "\\") replace(name, "/", "\\") else replace(name, "\\", "/")
  }

  /** Replaces characters unsuitable for filenames (/, \\, :, *, ?, etc.) with spaces.
   *
   * @param name the string to purify
   * @return the purified string safe for use as filename
   */
  def purify(name: String): String = {
    var result = name
    reservedChars foreach { c =>
      result = result.replace(c, ' ')
    }
    result
  }

  /** Returns a File for the given path, expanding tilde and resolving to absolute path.
   *
   * @param name the file path (supports ~ for home, ~+ for pwd)
   * @return the absolute File
   */
  def forName(name: String): File = new File(expandTilde(name)).getAbsoluteFile

  /** Returns a File for the given path relative to pwd, or absolute if path starts with / or :.
   *
   * @param pwd      the working directory for relative paths
   * @param filePath the file path (relative or absolute)
   * @return the resolved File
   */
  def forName(pwd: String, filePath: String): File = {
    val path = expandTilde(filePath)
    if / == "\\" && path.contains(":") then new File(path) //windows
    else if / == "/" && path.charAt(0) == '/' then new File(path) //linux
    else new File(pwd + / + path)
  }

  /** Expands tilde in path: ~/ for user home directory, ~+ for working directory.
   *
   * @param path the path containing tilde
   * @return the expanded path
   */
  def expandTilde(path: String): String = {
    val p = fileName(path)
    if p.startsWith("~" + /) then SystemInfo.user.home + p.substring(1)
    else if p.startsWith("~+") then SystemInfo.user.dir + p.substring(2)
    else p
  }

  /** Creates a BufferedWriter for the file with the specified charset.
   *
   * @param file    the file to write
   * @param charset the charset (default UTF-8)
   * @return Writer for the file
   */
  def stringWriter(file: File, charset: Charset = UTF_8): Writer = {
    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset))
  }

  /** Reads the contents of a file into a String. The file is always closed.
   *
   * @param file    the file to read
   * @param charset the charset for decoding (default UTF-8)
   * @return the file contents as string
   */
  def readString(file: File, charset: Charset = UTF_8): String = {
    var in: InputStream = null
    try {
      in = new FileInputStream(file)
      val sw = new StringBuilderWriter(16)
      IOs.copy(new InputStreamReader(in, charset), sw)
      sw.toString
    } finally
      IOs.close(in)
  }

  /** Writes a String to a file, creating the file if it does not exist.
   *
   * @param file    the file to write to
   * @param data    the string content to write
   * @param charset the charset for encoding (default UTF-8)
   */
  def writeString(file: File, data: String, charset: Charset = UTF_8): Unit = {
    var out: OutputStream = null
    try {
      out = writeOpen(file)
      IOs.write(data, out, charset)
      out.close()
    } finally
      IOs.close(out)
  }

  /** Updates the file's last modified time. Creates the file if it does not exist.
   *
   * @param file the file to touch
   */
  def touch(file: File): Unit = {
    if (!file.exists()) IOs.close(writeOpen(file))
    val success = file.setLastModified(System.currentTimeMillis)
    if (!success) throw new IOException("Unable to set the last modification time for " + file)
  }

  /** Opens a file for writing, creating parent directories if needed.
   *
   * @param file   the file to open
   * @param append if true, append to existing content
   * @return the FileOutputStream
   */
  def writeOpen(file: File, append: Boolean = false): FileOutputStream = {
    if (file.exists()) {
      if (file.isDirectory) throw new IOException("File '" + file + "' exists but is a directory")
      if (!file.canWrite) throw new IOException("File '" + file + "' cannot be written to")
    } else {
      val parent = file.getParentFile
      if (parent != null)
        if (!parent.mkdirs() && !parent.isDirectory)
          throw new IOException("Directory '" + parent + "' could not be created")
    }
    new FileOutputStream(file, append)
  }

  /** Reads the contents of a file line by line. The file is always closed.
   *
   * @param file    the file to read
   * @param charset the charset for decoding (default UTF-8)
   * @return the list of lines
   */
  def readLines(file: File, charset: Charset = UTF_8): List[String] = {
    var in: InputStream = null
    try {
      in = new FileInputStream(file)
      if null == charset then IOs.readLines(new InputStreamReader(in))
      else IOs.readLines(new InputStreamReader(in, charset))
    } finally
      IOs.close(in)
  }

  /** Copies a file to a new location preserving the file date.
   * Creates destination directory if needed. Overwrites if destination exists.
   * Tries to preserve last modified date but success is not guaranteed.
   *
   * @param src  the source file to copy, must not be null
   * @param dest the destination file, must not be null
   */
  @throws[IOException]("if source or destination is invalid or an IO error occurs during copying")
  def copy(src: File, dest: File): Unit = {
    require(null != src)
    require(null != dest)
    if !src.exists then throw new FileNotFoundException("Source '" + src + "' does not exist")
    if src.isDirectory then throw new IOException("Source '" + src + "' exists but is a directory")
    if src.getCanonicalPath == dest.getCanonicalPath then throw new IOException(s"Source '${src}' and destination '${dest}' are the same")
    val parentFile = dest.getParentFile
    if (parentFile != null)
      if (!parentFile.mkdirs() && !parentFile.isDirectory)
        throw new IOException("Destination '" + parentFile + "' directory cannot be created")
    if (dest.exists()) {
      if dest.isDirectory then throw new IOException("Destination '" + dest + "' exists but is a directory")
      if !dest.canWrite then throw new IOException("Destination '" + dest + "' exists but is read-only")
    }
    doCopy(src, dest, true)
  }

  private def doCopy(srcFile: File, destFile: File, preserveFileDate: Boolean): Unit = {
    var fis: FileInputStream = null
    var fos: FileOutputStream = null
    var input: FileChannel = null
    var output: FileChannel = null
    try {
      fis = new FileInputStream(srcFile)
      fos = new FileOutputStream(destFile)
      input = fis.getChannel
      output = fos.getChannel
      val size = input.size
      var pos = 0L
      var count = 0L
      while (pos < size) {
        count = if (size - pos > copyBufferSize) copyBufferSize else size - pos
        pos += output.transferFrom(input, pos, count)
      }
    } finally {
      IOs.close(output, fos, input, fis)
    }
    if (srcFile.length != destFile.length)
      throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'")
    if preserveFileDate then destFile.setLastModified(srcFile.lastModified())
  }

  /** Sets the file (and children if directory) to read-only.
   *
   * @param file the file or directory
   */
  def setReadOnly(file: File): Unit = {
    if (file.exists() && file.canWrite)
      travel(file, x => x.setReadOnly())
  }

  /** Sets the file (and children if directory) to writable.
   *
   * @param file the file or directory
   */
  def setWriteable(file: File): Unit = {
    if (file.exists() && !JFiles.isWritable(file.toPath))
      travel(file, x => x.setWritable(true))
  }

  /** Sets the file (and children if directory) to executable.
   *
   * @param file the file or directory
   */
  def setExecutable(file: File): Unit = {
    if (file.exists() && !JFiles.isExecutable(file.toPath))
      travel(file, x => x.setExecutable(true))
  }

  /** Recursively applies the function to the file and its descendants (excluding hidden/symlinks).
   *
   * @param file         the root file or directory
   * @param attributeSet the function to apply to each file
   */
  def travel(file: File, attributeSet: File => Unit): Unit = {
    attributeSet(file)
    if (file.isDirectory && !file.isHidden && !JFiles.isSymbolicLink(file.toPath))
      file.listFiles() foreach { child =>
        travel(child, attributeSet)
      }
  }

  /** Deletes the file or directory recursively.
   *
   * @param file the file or directory to delete
   */
  def remove(file: File): Unit = {
    if file.isFile then file.delete()
    Files.travel(file, f => f.delete())
    file.delete()
  }

  /** Clears the directory by deleting all descendants, or deletes the file.
   *
   * @param file the file or directory to clear
   */
  def clear(file: File): Unit = {
    if file.isFile then file.delete()
    Files.travel(file, f => f.delete())
  }

}
