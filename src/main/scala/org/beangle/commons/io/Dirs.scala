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

import org.beangle.commons.io.Files./

import java.io.File
import java.nio.file.Paths

/** Directory operations (list, delete, mkdir). */
object Dirs {

  /** Creates Dirs for the given path.
   *
   * @param path the directory path
   * @return Dirs instance
   */
  def on(path: String): Dirs = new Dirs(new File(path))

  /** Creates Dirs for the given file.
   *
   * @param file the directory file
   * @return Dirs instance
   */
  def on(file: File): Dirs = new Dirs(file)

  /** Creates Dirs for the child under the given file.
   *
   * @param file  the parent directory
   * @param child the child path
   * @return Dirs instance for the child
   */
  def on(file: File, child: String): Dirs = new Dirs(new File(file, child))

  /** Deletes the file or directory (recursive for directories).
   *
   * @param file the file or directory to delete
   */
  def delete(file: File): Unit = {
    if isLink(file) then
      file.delete()
    else if file.exists() then
      remove(file)
  }

  /** Returns true if the file is a symbolic link.
   *
   * @param file the file to check
   * @return true if symbolic link
   */
  def isLink(file: File): Boolean = java.nio.file.Files.isSymbolicLink(file.toPath)

  private def remove(file: File): Unit = {
    if file.exists() then
      if file.isDirectory then
        if file.list().length == 0 || isLink(file) then
          file.delete()
        else {
          //list all the directory contents
          val files = file.list()
          var i = 0
          while (i < files.length) {
            val tmp = files(i)
            remove(new File(file, tmp))
            i += 1
          }
          if (file.list().length == 0) file.delete()
        }
      else file.delete()
  }
}

/** Directory operations helper. */
class Dirs(val pwd: File) {
  require(!pwd.exists() || pwd.isDirectory)

  /** Returns true if the directory exists. */
  def exists: Boolean = pwd.exists()

  /** Renames the directory to the new name.
   *
   * @param newName the new directory name
   * @return Dirs for the renamed directory
   */
  def rename(newName: String): Dirs = {
    val dest = new File(pwd.getParentFile.getAbsolutePath + / + newName)
    pwd.renameTo(dest)
    new Dirs(dest)
  }

  /** Deletes the specified children under this directory.
   *
   * @param children the child names to delete
   * @return this for chaining
   */
  def delete(children: String*): this.type = {
    children foreach { child =>
      Dirs.delete(new File(pwd, child))
    }
    this
  }

  /** Creates directories (this dir if children empty, else child dirs).
   *
   * @param children child names to create (empty = create this dir)
   * @return this for chaining
   */
  def mkdirs(children: String*): this.type = {
    if (children.isEmpty)
      pwd.mkdirs()
    else
      children foreach { child =>
        new File(pwd, child).mkdirs()
      }
    this
  }

  /** Lists file and directory names in this directory. */
  def ls(): Seq[String] = {
    val i = pwd.list()
    if null == i then List.empty else i.toSeq
  }

  /** Returns Dirs for the child path.
   *
   * @param child the child directory name
   * @return Dirs for the child
   */
  def cd(child: String): Dirs = {
    new Dirs(new File(pwd, child))
  }

  /** Creates the file if it does not exist (creates parent dirs too).
   *
   * @param child the child file name
   * @return this for chaining
   */
  def touch(child: String): this.type = {
    val file = new File(pwd, child)
    if (!file.exists()) {
      file.getParentFile.mkdirs()
      Files.touch(file)
    }
    this
  }

  /** Creates a symbolic link to the target (link name = target name).
   *
   * @param target the target file path
   * @return this for chaining
   */
  def ln(target: String): this.type = ln(new File(target))

  /** Creates a symbolic link to the target (link name = target name).
   *
   * @param target the target file
   * @return this for chaining
   */
  def ln(target: File): this.type = ln(target, target.getName)

  /** Sets this directory and its children to read-only.
   *
   * @return this for chaining
   */
  def setReadOnly(): this.type = {
    Files.setReadOnly(pwd)
    this
  }

  /** Sets this directory and its children to writable.
   *
   * @return this for chaining
   */
  def setWriteable(): this.type = {
    Files.setWriteable(pwd)
    this
  }

  /** Creates a symbolic link to the target with the given link name.
   *
   * @param target  the target file
   * @param newName the link name in this directory
   * @return this for chaining
   */
  def ln(target: File, newName: String): this.type = {
    if (!target.exists()) throw new RuntimeException("Cannot find target " + target.getAbsolutePath)
    val link = new File(pwd, newName)
    if (java.nio.file.Files.isSymbolicLink(Paths.get(link.toURI))) link.delete()
    if (link.exists())
      throw new RuntimeException("Cannot make link on existed file.")
    else
      java.nio.file.Files.createSymbolicLink(Paths.get(link.toURI), Paths.get(target.toURI))
    this
  }

  /** Writes content to the file (creates file and parent dirs if needed).
   *
   * @param fileName the file name in this directory
   * @param content  the content to write
   * @return this for chaining
   */
  def write(fileName: String, content: String): this.type = {
    touch(fileName)
    Files.writeString(new File(pwd, fileName), content)
    this
  }

  /** Copies a file into this directory (keeps original name).
   *
   * @param file the source file path
   * @return this for chaining
   */
  def copyFrom(file: String): this.type = {
    val src = new File(file)
    Files.copy(src, new File(pwd, src.getName))
    this
  }
}
