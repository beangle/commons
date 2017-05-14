package org.beangle.commons.cache.caffeine

import org.beangle.commons.cache.AbstractCacheManager
import org.beangle.commons.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.{ Cache => CCache }
import java.util.concurrent.TimeUnit
import org.beangle.commons.collection.Properties
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders

class CaffeineCacheManager(autoCreate: Boolean = false) extends AbstractCacheManager(autoCreate) {

  val specs = ClassLoaders.getResource("META-INF/caffeine.properties") match {
    case Some(p) => IOs.readProperties(p)
    case None    => Map.empty[String, String]
  }

  var maximumSize: Int = 10000

  var ttl: Int = 60 * 60

  var tti: Int = 15 * 60

  protected override def newCache[K, V](name: String, keyType: Class[K], valueType: Class[V]): Cache[K, V] = {
    if (specs.contains(name)) {
      val store: CCache[K, V] = Caffeine.from(specs(name)).build().asInstanceOf[CCache[K, V]]
      new CaffeineCache(store)
    } else {
      val store: CCache[K, V] = Caffeine.newBuilder().maximumSize(maximumSize).expireAfterWrite(ttl, TimeUnit.SECONDS)
        .expireAfterAccess(tti, TimeUnit.SECONDS).build().asInstanceOf[CCache[K, V]]
      new CaffeineCache(store)
    }
  }

  protected def findCache[K, V](name: String, keyType: Class[K], valueType: Class[V]): Cache[K, V] = {
    if (specs.contains(name)) {
      val store: CCache[K, V] = Caffeine.from(specs(name)).build().asInstanceOf[CCache[K, V]]
      new CaffeineCache(store)
    } else {
      null
    }
  }

  override def destroy(): Unit = {
  }
}
