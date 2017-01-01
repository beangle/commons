package org.beangle.commons.cache.caffeine

import org.beangle.commons.cache.Cache

import com.github.benmanes.caffeine.cache.{ Cache => CCache }
import org.beangle.commons.lang.annotation.value
import org.beangle.commons.lang.Objects
import java.util.concurrent.TimeUnit

class CaffeineCache[K, V](store: CCache[K, V]) extends Cache[K, V] {

  override def get(key: K): Option[V] = {
    Option(store.getIfPresent(key))
  }

  override def put(key: K, value: V): Unit = {
    store.put(key, value)
  }

  override def touch(key: K): Boolean = {
    val ele = store.getIfPresent(key)
    if (null == ele) {
      false
    } else {
      store.put(key, ele)
      true
    }
  }

  override def exists(key: K): Boolean = {
    store.getIfPresent(key) != null
  }

  override def putIfAbsent(key: K, value: V): Boolean = {
    val existed = store.getIfPresent(key)
    if (null == existed) {
      store.put(key, value)
      true
    } else {
      false
    }
  }

  override def replace(key: K, value: V): Option[V] = {
    val oldValue = store.getIfPresent(key)
    if (null == oldValue) {
      None
    } else {
      store.put(key, value)
      Option(oldValue)
    }
  }

  override def replace(key: K, oldvalue: V, newvalue: V): Boolean = {
    val existed = store.getIfPresent(key)
    if (Objects.equals(existed, oldvalue)) {
      store.put(key, newvalue)
      true
    } else {
      false
    }
  }

  override def evict(key: K): Boolean = {
    val existed = store.getIfPresent(key)
    if (null != existed) store.invalidate(key)
    existed != null
  }

  override def clear(): Unit = {
    store.cleanUp()
  }

  override def tti: Int = {
    val expiration = store.policy().expireAfterAccess()
    if (expiration.isPresent()) expiration.get.getExpiresAfter(TimeUnit.SECONDS).asInstanceOf[Int]
    else -1
  }

  override def ttl: Int = {
    val expiration = store.policy().expireAfterWrite()
    if (expiration.isPresent()) expiration.get.getExpiresAfter(TimeUnit.SECONDS).asInstanceOf[Int]
    else -1
  }
}
