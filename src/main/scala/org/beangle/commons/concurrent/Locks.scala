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

package org.beangle.commons.concurrent

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.{Condition, Lock, ReadWriteLock}

/** Utility for simplifying lock acquire and release.
 *
 * @author beangle
 */
object Locks {

  /** Executes the block with the lock held. Always releases lock on completion.
   *
   * @param lock  the lock to acquire
   * @param block the block to run under the lock
   * @return the result of the block
   */
  def withLock[T](lock: Lock)(block: => T): T = {
    lock.lock()
    try block finally lock.unlock()
  }

  /** Interruptible version: responds to interrupt while waiting for lock.
   *
   * @param lock  the lock to acquire
   * @param block the block to run under the lock
   * @return the result of the block
   */
  @throws[InterruptedException]
  def withLockInterruptibly[T](lock: Lock)(block: => T): T = {
    lock.lockInterruptibly()
    try block finally lock.unlock()
  }

  /** Timeout version: returns None if lock not acquired within the specified time.
   *
   * @param lock    the lock to acquire
   * @param timeout the timeout duration
   * @param unit    the time unit
   * @param block   the block to run under the lock
   * @return Some(result) if lock acquired, None if timed out
   */
  def withTryLock[T](lock: Lock, timeout: Long, unit: TimeUnit)(block: => T): Option[T] = {
    if (lock.tryLock(timeout, unit)) {
      try Some(block) finally lock.unlock()
    } else {
      None // Lock not acquired within timeout
    }
  }

  /** Read lock: for read-heavy scenarios, allows multiple readers.
   *
   * @param rwLock the read-write lock
   * @param block  the block to run under read lock
   * @return the result of the block
   */
  def withReadLock[T](rwLock: ReadWriteLock)(block: => T): T = {
    withLock(rwLock.readLock())(block)
  }

  /** Write lock: for write operations, exclusive access.
   *
   * @param rwLock the read-write lock
   * @param block  the block to run under write lock
   * @return the result of the block
   */
  def withWriteLock[T](rwLock: ReadWriteLock)(block: => T): T = {
    withLock(rwLock.writeLock())(block)
  }

  /** Waits for condition: releases lock, waits until condition is true, then re-acquires lock.
   *
   * @param condition the condition bound to the lock
   * @param until     wait until this predicate is true
   */
  @throws[InterruptedException]
  def awaitCondition[T](lock: Lock, condition: Condition)(until: => Boolean)(block: => T): T = {
    withLock(lock) {
      while (!until) { // Must use while to prevent spurious wakeup
        condition.await()
      }
      block
    }
  }

}
