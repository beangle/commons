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

/**
 * 锁简易实用类
 * 可以简化Lock的锁定和释放
 */
object Locks {

  def withLock[T](lock: Lock)(block: => T): T = {
    lock.lock()
    try block finally lock.unlock()
  }

  // 可中断版本：等待锁时响应中断
  @throws[InterruptedException]
  def withLockInterruptibly[T](lock: Lock)(block: => T): T = {
    lock.lockInterruptibly()
    try block finally lock.unlock()
  }

  // 超时版本：指定等待时间，超时返回 None
  def withTryLock[T](lock: Lock, timeout: Long, unit: TimeUnit)(block: => T): Option[T] = {
    if (lock.tryLock(timeout, unit)) {
      try Some(block) finally lock.unlock()
    } else {
      None // 超时未获取锁，返回 None
    }
  }

  /**
   * 读锁：适用于读多写少场景，允许多个线程同时读
   */
  def withReadLock[T](rwLock: ReadWriteLock)(block: => T): T = {
    withLock(rwLock.readLock())(block)
  }

  /**
   * 写锁：适用于写操作，独占锁
   */
  def withWriteLock[T](rwLock: ReadWriteLock)(block: => T): T = {
    withLock(rwLock.writeLock())(block)
  }

  /**
   * 条件等待：等待某个条件满足，自动释放锁并等待，唤醒后重新获取锁
   *
   * @param condition 绑定在锁上的条件
   * @param until     等待直到条件为真
   */
  @throws[InterruptedException]
  def awaitCondition[T](lock: Lock, condition: Condition)(until: => Boolean)(block: => T): T = {
    withLock(lock) {
      while (!until) {// 必须使用 while 循环防止虚假唤醒
        condition.await()
      }
      block
    }
  }

}
