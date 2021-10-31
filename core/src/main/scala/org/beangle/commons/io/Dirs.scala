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

object Dirs {

  def on(path: String): Dirs = new Dirs(new File(path))

  def on(file: File): Dirs = new Dirs(file)

  def on(file: File, child: String): Dirs = new Dirs(new File(file, child))

  def delete(file: File): Unit = {
    if isLink(file) then
      file.delete()
    else if file.exists() then
      remove(file)
  }

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

class Dirs(val pwd: File) {
  require(!pwd.exists() || pwd.isDirectory)

  def exists: Boolean = pwd.exists()

  def rename(newName: String): Dirs = {
    val dest = new File(pwd.getParentFile.getAbsolutePath + / + newName)
    pwd.renameTo(dest)
    new Dirs(dest)
  }

  def delete(children: String*): this.type = {
    children foreach { child =>
      Dirs.delete(new File(pwd, child))
    }
    this
  }

  def mkdirs(children: String*): this.type = {
    if (children.isEmpty)
      pwd.mkdirs()
    else
      children foreach { child =>
        new File(pwd, child).mkdirs()
      }
    this
  }

  def ls(): Seq[String] =
    pwd.list().toSeq

  def cd(child: String): Dirs =
    new Dirs(new File(pwd, child))

  def touch(child: String): this.type = {
    val file = new File(pwd, child)
    if (!file.exists()) {
      file.getParentFile.mkdirs()
      Files.touch(file)
    }
    this
  }

  def ln(target: String): this.type =
    ln(new File(target))

  def ln(target: File): this.type =
    ln(target, target.getName)

  def setReadOnly(): this.type = {
    Files.setReadOnly(pwd)
    this
  }

  def setWriteable(): this.type = {
    Files.setWriteable(pwd)
    this
  }

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

  def write(fileName: String, content: String): this.type = {
    touch(fileName)
    Files.writeString(new File(pwd, fileName), content)
    this
  }

  def copyFrom(file: String): this.type = {
    val src = new File(file)
    Files.copy(src, new File(pwd, src.getName))
    this
  }
}
