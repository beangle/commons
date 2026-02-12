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

package org.beangle.commons.image

import org.beangle.commons.lang.Strings

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.{File, FileOutputStream, OutputStream}
import javax.imageio.ImageIO

/** Image manipulation utilities. */
object Images {

  /** Rotates image and writes to destination file. */
  def rotate(src: File, dest: File, degree: Int): Unit = {
    rotate(src, new FileOutputStream(dest), degree)
  }

  /** Rotates image by degree and writes to stream. */
  def rotate(src: File, dest: OutputStream, degree: Int): Unit = {
    try {
      val buf = ImageIO.read(src)
      val typeName = Strings.substringAfterLast(src.getName, ".").toUpperCase()
      val width = buf.getWidth
      val height = buf.getHeight
      val output = new BufferedImage(height, width, buf.getType)
      val g2d = output.getGraphics.asInstanceOf[Graphics2D]
      g2d.translate(height / 2.0, width / 2.0)
      g2d.rotate(Math.PI * (degree / 180.0))
      g2d.translate(-width / 2.0, -height / 2.0)
      g2d.drawImage(buf, 0, 0, width, height, null)
      ImageIO.write(output, typeName, dest)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }
}
