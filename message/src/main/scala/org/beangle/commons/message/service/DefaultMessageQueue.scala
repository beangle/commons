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
package org.beangle.commons.message.service

import org.beangle.commons.message.MessageQueue
import org.beangle.commons.message.Message
import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue
import java.util.List
import scala.collection.JavaConversions._

class DefaultMessageQueue[T <: Message] extends MessageQueue[T] {

  private var queue: Queue[T] = new LinkedBlockingQueue[T]

  def getMessages(): List[T] = new java.util.ArrayList[T](queue)

  def addMessage(message: T) {
    queue.add(message)
  }

  def addMessages(contexts: List[T]) {
    queue.addAll(contexts)
  }

  def poll(): T = queue.poll()

  def size(): Int = queue.size()
}
