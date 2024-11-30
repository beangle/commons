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
