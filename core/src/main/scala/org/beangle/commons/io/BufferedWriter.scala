package org.beangle.commons.io

import java.io.Writer

class BufferedWriter(out: Writer, bufferSize: Int = 8192) extends Writer {
  private var buffer = new Array[Char](bufferSize)
  private var pointer: Int = _

  override def write(c: Int): Unit = {
    if (pointer + 1 >= buffer.length) flushBuffer()
    buffer(pointer) = c.asInstanceOf[Char]
    pointer += 1
  }

  private def flushBuffer(): Unit = {
    if (pointer == 0) return ;
    out.write(buffer, 0, pointer);
    pointer = 0
  }

  override def write(c: Array[Char], off: Int, len: Int): Unit = {
    if (pointer + len >= buffer.length) {
      flushBuffer()
      if (len > buffer.length) {
        out.write(c, off, len);
        return
      }
    }
    System.arraycopy(c, 0, buffer, pointer, len);
    pointer += len
  }

  override def flush(): Unit = {
    flushBuffer()
    out.flush()
  }

  override def close(): Unit = {
    flushBuffer()
    out.close()
  }
}