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

package org.beangle.commons.file.watch

import com.swoval.files.{FileTreeViews, PathWatchers}
import org.beangle.commons.collection.Collections
import org.beangle.commons.file.watch.FileWatcher.{Observer, Reply, WatcherPath}
import org.beangle.commons.regex.AntPathPattern

import java.nio.file.{Path, Paths}

/** File Watcher
 * {{{
 *       val watcher = FileWatcher.newBuilder().add("D:\\tmp","*.jpg","*.png")
 *         .build((kind: String, path: Path) => {
 *           println(s"$kind $path")
 *         })
 *       try {
 *         Thread.sleep(1000000) // ensure the callback fires
 *       } finally watcher.close()
 * }}}
 *
 * @param paths
 * @param o
 */
class FileWatcher private(paths: Iterable[WatcherPath], o: Observer) extends AutoCloseable {
  val watcher = com.swoval.files.PathWatchers.get(true)
  paths foreach { path => watcher.register(path.path, Integer.MAX_VALUE) }
  watcher.addObserver(new Reply(o, paths))

  def close(): Unit = {
    watcher.close()
  }
}

object FileWatcher {
  def newBuilder(): Builder = new Builder

  @FunctionalInterface
  trait Observer {
    def onChange(kind: String, path: Path): Unit
  }

  class Builder {
    private val paths = Collections.newBuffer[WatcherPath]

    def add(path: Path, patterns: AntPathPattern*): Builder = {
      paths.addOne(WatcherPath(path, patterns: _*))
      this
    }

    def add(path: String, patterns: String*): Builder = {
      paths.addOne(WatcherPath(Paths.get(path), patterns.map(x => new AntPathPattern(x)): _*))
      this
    }

    def build(o: Observer): FileWatcher = {
      new FileWatcher(paths, o)
    }
  }

  private case class WatcherPath(path: Path, patterns: AntPathPattern*)

  /** File event reply
   *
   * @param o
   * @param paths
   */
  private class Reply(o: Observer, paths: Iterable[WatcherPath]) extends FileTreeViews.Observer[PathWatchers.Event] {

    val hasFilter = paths.exists(_.patterns.nonEmpty)

    def onError(t: Throwable): Unit = {}

    def onNext(e: PathWatchers.Event): Unit = {
      e.getKind match
        case PathWatchers.Event.Kind.Error | PathWatchers.Event.Kind.Overflow =>
        case _ =>
          val path = e.getTypedPath.getPath
          if (hasFilter) {
            paths.find(r => path.startsWith(r.path)) foreach { wp =>
              if wp.patterns.isEmpty || wp.patterns.exists(_.matches(path.getFileName.toString)) then
                o.onChange(e.getKind.toString, path)
            }
          } else {
            o.onChange(e.getKind.toString, path)
          }
    }
  }
}
