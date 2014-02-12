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
package org.beangle.commons.io

import java.io._
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import org.beangle.commons.lang.Assert

object Files {

  val CopyBufferSize = 1024 * 1024 * 30

  /**
   * Reads the contents of a file into a String using the default encoding for the VM.
   * The file is always closed.
   */
  def readFileToString(file: File): String = readFileToString(file, null)

  /**
   * Reads the contents of a file into a String.
   * The file is always closed.
   */
  def readFileToString(file: File, charset: Charset): String = {
    var in: InputStream = null
    try {
      in = new FileInputStream(file)
      val sw = new StringBuilderWriter(16)
      if (null == charset) IOs.copy(new InputStreamReader(in), sw) else IOs.copy(new InputStreamReader(in,
        charset.name()), sw)
      sw.toString
    } finally {
      IOs.close(in)
    }
  }

  /**
   * Writes a String to a file creating the file if it does not exist.
   */
  def writeStringToFile(file: File, data: String, charset: Charset = null) {
    var out: OutputStream = null
    try {
      out = openOutputStream(file, false)
      IOs.write(data, out, charset)
      out.close()
    } finally {
      IOs.close(out)
    }
  }

  private def openOutputStream(file: File, append: Boolean): FileOutputStream = {
    if (file.exists()) {
      if (file.isDirectory) throw new IOException("File '" + file + "' exists but is a directory")
      if (!file.canWrite) throw new IOException("File '" + file + "' cannot be written to")
    } else {
      val parent = file.getParentFile
      if (parent != null) {
        if (!parent.mkdirs() && !parent.isDirectory())
          throw new IOException("Directory '" + parent + "' could not be created");
      }
    }
    new FileOutputStream(file, append)
  }

  /**
   * Reads the contents of a file line by line to a List of Strings.
   * The file is always closed.
   */
  def readLines(file: File, charset: Charset = null): List[String] = {
    var in: InputStream = null
    try {
      in = new FileInputStream(file)
      if (null == charset) {
        IOs.readLines(new InputStreamReader(in))
      } else {
        val reader = new InputStreamReader(in, charset.name())
        IOs.readLines(reader)
      }
    } finally {
      IOs.close(in)
    }
  }

  def readLines(file: File): List[String] = readLines(file, null)

  /**
   * Copies a file to a new location preserving the file date.
   * <p>
   * This method copies the contents of the specified source file to the specified destination file.
   * The directory holding the destination file is created if it does not exist. If the destination
   * file exists, then this method will overwrite it.
   * <p>
   * <strong>Note:</strong> This method tries to preserve the file's last modified date/times using
   * {@link File#setLastModified(long)}, however it is not guaranteed that the operation will
   * succeed. If the modification operation fails, no indication is provided.
   *
   * @param srcFile an existing file to copy, must not be <code>null</code>
   * @param destFile the new file, must not be <code>null</code>
   * @throws NullPointerException if source or destination is <code>null</code>
   * @throws IOException if source or destination is invalid
   * @throws IOException if an IO error occurs during copying
   * @see #copyFileToDirectory(File, File)
   */
  def copyFile(srcFile: File, destFile: File) {
    null != srcFile
    null != destFile
    if (srcFile.exists() == false) {
      throw new FileNotFoundException("Source '" + srcFile + "' does not exist")
    }
    if (srcFile.isDirectory) {
      throw new IOException("Source '" + srcFile + "' exists but is a directory")
    }
    if (srcFile.getCanonicalPath == destFile.getCanonicalPath) {
      throw new IOException("Source '" + srcFile + "' and destination '" + destFile +
        "' are the same")
    }
    val parentFile = destFile.getParentFile
    if (parentFile != null) {
      if (!parentFile.mkdirs() && !parentFile.isDirectory) {
        throw new IOException("Destination '" + parentFile + "' directory cannot be created")
      }
    }
    if (destFile.exists()) {
      if (destFile.isDirectory) {
        throw new IOException("Destination '" + destFile + "' exists but is a directory")
      }
      if (!destFile.canWrite()) throw new IOException("Destination '" + destFile + "' exists but is read-only")
    }
    doCopyFile(srcFile, destFile, true)
  }

  private def doCopyFile(srcFile: File, destFile: File, preserveFileDate: Boolean) {
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
        count = if (size - pos > CopyBufferSize) CopyBufferSize else size - pos
        pos += output.transferFrom(input, pos, count)
      }
    } finally {
      IOs.close(output)
      IOs.close(fos)
      IOs.close(input)
      IOs.close(fis)
    }
    if (srcFile.length != destFile.length) {
      throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" +
        destFile +
        "'")
    }
    if (preserveFileDate) {
      destFile.setLastModified(srcFile.lastModified())
    }
  }
}
