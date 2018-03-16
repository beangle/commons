/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.file.diff

import java.io.{ BufferedInputStream, BufferedOutputStream, ByteArrayInputStream, ByteArrayOutputStream, File, FileInputStream, FileOutputStream, IOException, InputStream, OutputStream }

import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.beangle.commons.file.diff.bsdiff.{ Format, Offset, SuffixSort }
import org.beangle.commons.io.Files

/**
 * This module provides functionality for generating bsdiff patches from two
 * source files (an old and new file).
 *
 * The algorithm based on bsdiff(http://www.daemonology.net/bsdiff/)
 * and transform from https://github.com/malensek/jbsdiff
 */
object Bsdiff {

  def diff(oldFile: File, newFile: File, patchFile: File): Unit = {
    val oldIn = new FileInputStream(oldFile)
    val oldBytes = new Array[Byte](oldFile.length.asInstanceOf[Int])
    oldIn.read(oldBytes)
    oldIn.close()

    val newIn = new FileInputStream(newFile)
    val newBytes = new Array[Byte](newFile.length.asInstanceOf[Int])
    newIn.read(newBytes)
    newIn.close()

    Files.touch(patchFile)
    val out = new FileOutputStream(patchFile)
    diff(oldBytes, newBytes, out)
    out.close()
  }

  def diff(oldBytes: Array[Byte], newBytes: Array[Byte], out: OutputStream): Unit = {
    val compressor = new CompressorStreamFactory()

    val I = sort(oldBytes)

    val byteOut = new ByteArrayOutputStream()
    var patchOut = compressor.createCompressorOutputStream(Format.Compression, byteOut)

    var scan, len, position = 0
    var lastScan, lastPos, lastOffset = 0
    var oldScore, scsc = 0
    var s, Sf, lenf, Sb, lenb = 0
    var overlap, Ss, lens = 0

    val db = new Array[Byte](newBytes.length + 1)
    val eb = new Array[Byte](newBytes.length + 1)
    var dblen, eblen = 0

    while (scan < newBytes.length) {
      oldScore = 0

      scan += len
      scsc = scan
      var running = true
      while (scan < newBytes.length && running) {
        val result = SuffixSort.search(I, oldBytes, 0, newBytes, scan, 0, oldBytes.length)
        len = result.length
        position = result.position

        while (scsc < scan + len) {
          if ((scsc + lastOffset < oldBytes.length) && (oldBytes(scsc + lastOffset) == newBytes(scsc)))
            oldScore += 1
          scsc += 1
        }

        if (((len == oldScore) && (len != 0)) || (len > oldScore + 8)) {
          running = false
        } else {
          if ((scan + lastOffset < oldBytes.length) && (oldBytes(scan + lastOffset) == newBytes(scan)))
            oldScore -= 1
          scan += 1
        }
      }

      if ((len != oldScore) || (scan == newBytes.length)) {
        s = 0
        Sf = 0
        lenf = 0
        var i = 0
        while ((lastScan + i < scan) && (lastPos + i < oldBytes.length)) {
          if (oldBytes(lastPos + i) == newBytes(lastScan + i)) {
            s += 1
          }

          i += 1
          if (s * 2 - i > Sf * 2 - lenf) {
            Sf = s
            lenf = i
          }
        }

        lenb = 0
        if (scan < newBytes.length) {
          s = 0
          Sb = 0
          var i = 1
          while ((scan >= lastScan + i) &&
            (position >= i)) {
            if (oldBytes(position - i) == newBytes(scan - i)) {
              s += 1
            }
            if (s * 2 - i > Sb * 2 - lenb) {
              Sb = s
              lenb = i
            }
            i += 1
          }
        }

        if (lastScan + lenf > scan - lenb) {
          overlap = (lastScan + lenf) - (scan - lenb)
          s = 0
          Ss = 0
          lens = 0
          var i = 0
          while (i < overlap) {
            if (newBytes(lastScan + lenf - overlap + i) == oldBytes(lastPos + lenf - overlap + i)) {
              s += 1
            }
            if (newBytes(scan - lenb + i) == oldBytes(position - lenb + i)) {
              s -= 1
            }
            if (s > Ss) {
              Ss = s
              lens = i + 1
            }
            i += 1
          }
          lenf += lens - overlap
          lenb -= lens
        }

        i = 0
        while (i < lenf) {
          db(dblen + i) = (db(dblen + i) | (newBytes(lastScan + i) - oldBytes(lastPos + i))).asInstanceOf[Byte]
          i += 1
        }

        i = 0
        while (i < (scan - lenb) - (lastScan + lenf)) {
          eb(eblen + i) = newBytes(lastScan + lenf + i)
          i += 1
        }

        dblen += lenf
        eblen += (scan - lenb) - (lastScan + lenf)

        val b = Format.Block(lenf, (scan - lenb) - (lastScan + lenf), (position - lenb) -
          (lastPos + lenf))
        Offset.writeBlock(b, patchOut)

        lastScan = scan - lenb
        lastPos = position - lenb
        lastOffset = position - scan
      }
    }

    /* Done writing control blocks */
    patchOut.close()

    val controlLength = byteOut.size
    patchOut = compressor.createCompressorOutputStream(Format.Compression, byteOut)
    patchOut.write(db)
    patchOut.close()
    val diffLength = byteOut.size - controlLength

    patchOut = compressor.createCompressorOutputStream(Format.Compression, byteOut)
    patchOut.write(eb)
    patchOut.close()

    val header = Format.Header(controlLength, diffLength, newBytes.length)

    Offset.writeHeader(header, out)
    out.write(byteOut.toByteArray)
  }

  /**
   * Using an old file and its accompanying patch, this method generates a new
   * (updated) file and writes it to an {@link OutputStream}.
   *
   * @param old    the original ('old') state of the binary
   * @param patch  a binary patch file to apply to the old state
   * @param out    an {@link OutputStream} to write the patched binary to
   */
  def patch(old: Array[Byte], patch: Array[Byte], out: OutputStream): Unit = {
    val headerIn = new ByteArrayInputStream(patch)
    val header = Offset.readHeader(headerIn)
    headerIn.close()

    /* Set up InputStreams for reading different regions of the patch */
    var controlIn: InputStream = new ByteArrayInputStream(patch)
    var dataIn: InputStream = new ByteArrayInputStream(patch)
    var extraIn: InputStream = new ByteArrayInputStream(patch)

    try {
      /* Seek to the correct offsets in each stream */
      controlIn.skip(Format.HeaderLength)
      dataIn.skip(Format.HeaderLength + header.controlLength)
      extraIn.skip(Format.HeaderLength + header.controlLength +
        header.diffLength)

      /* Set up compressed streams */
      val compressor = new CompressorStreamFactory()
      controlIn = compressor.createCompressorInputStream(controlIn)
      dataIn = compressor.createCompressorInputStream(dataIn)
      extraIn = compressor.createCompressorInputStream(extraIn)

      /* Start patching */
      var newPointer, oldPointer = 0
      val output = new Array[Byte](header.outputLength)
      val outputLength = header.outputLength
      while (newPointer < outputLength) {
        val control = Offset.readBlock(controlIn)
        read(dataIn, output, newPointer, control.diffLength)

        /* Add old data to diff string */
        var i = 0
        while (i < control.diffLength) {
          if ((oldPointer + i >= 0) && oldPointer + i < old.length) {
            output(newPointer + i) = (output(newPointer + i) + old(oldPointer + i)).asInstanceOf[Byte]
          }
          i += 1
        }

        newPointer += control.diffLength
        oldPointer += control.diffLength

        /* Copy the extra string to the output */
        read(extraIn, output, newPointer, control.extraLength)

        newPointer += control.extraLength
        oldPointer += control.seekLength
      }

      out.write(output)

    } finally {
      controlIn.close()
      dataIn.close()
      extraIn.close()
    }
  }

  def patch(oldFile: File, newFile: File, patchFile: File): Unit = {
    val headerIn = new FileInputStream(patchFile)
    val header = Offset.readHeader(headerIn)
    headerIn.close()

    var controlIn: InputStream = new BufferedInputStream(new FileInputStream(patchFile))
    var dataIn: InputStream = new BufferedInputStream(new FileInputStream(patchFile))
    var extraIn: InputStream = new BufferedInputStream(new FileInputStream(patchFile))

    try {
      /* Seek to the correct offsets in each stream */
      controlIn.skip(Format.HeaderLength)
      dataIn.skip(Format.HeaderLength + header.controlLength)
      extraIn.skip(Format.HeaderLength + header.controlLength +
        header.diffLength)

      /* Set up compressed streams */
      val compressor = new CompressorStreamFactory()
      controlIn = compressor.createCompressorInputStream(controlIn)
      dataIn = compressor.createCompressorInputStream(dataIn)
      extraIn = compressor.createCompressorInputStream(extraIn)

      val oldStream = new FileInputStream(oldFile)
      val old = new Array[Byte](oldFile.length.asInstanceOf[Int])
      oldStream.read(old)
      oldStream.close()

      val out = new BufferedOutputStream(new FileOutputStream(newFile))

      /* Start patching */
      var newPointer, oldPointer = 0
      var outputLength = header.outputLength
      while (newPointer < outputLength) {
        val control = Offset.readBlock(controlIn)

        /* Read diff string */
        val diffLength = control.diffLength
        val extraLength = control.extraLength
        val output = new Array[Byte](diffLength + extraLength)
        read(dataIn, output, 0, diffLength)

        /* Add old data to diff string */
        var i = 0
        while (i < diffLength) {
          if ((oldPointer + i >= 0) && oldPointer + i < old.length) {
            output(i) = (output(i) + old(oldPointer + i)).asInstanceOf[Byte]
          }
          i += 1
        }

        newPointer += diffLength
        oldPointer += diffLength

        /* Copy the extra string to the output */
        read(extraIn, output, diffLength, extraLength)
        out.write(output)

        newPointer += extraLength
        oldPointer += control.seekLength
      }

      out.close()

    } finally {
      controlIn.close()
      dataIn.close()
      extraIn.close()
    }
  }

  /**
   * Reads data from an InputStream, and throws an {@link IOException} if
   * fewer bytes were read than requested.  Since the lengths of data in a
   * bsdiff patch are explicitly encoded in the control blocks, reading less
   * than expected is an unrecoverable error.
   */
  private def read(in: InputStream, dest: Array[Byte], off: Int, len: Int): Unit = {
    if (len == 0) return
    val read = in.read(dest, off, len)
    if (read < len) {
      throw new IOException("Corrupt patch bytes expected = " + len +
        " bytes read = " + read)
    }
  }
  private def sort(input: Array[Byte]): Array[Int] = {
    val I = new Array[Int](input.length + 1)
    val V = new Array[Int](input.length + 1)
    SuffixSort.qsufsort(I, V, input)
    I
  }
}
