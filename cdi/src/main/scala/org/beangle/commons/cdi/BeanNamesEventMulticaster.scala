package org.beangle.commons.cdi

import org.beangle.commons.event.DefaultEventMulticaster
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.bean.Initializing
import org.beangle.commons.event.EventListener

@description("依据名称查找监听者的事件广播器")
class BeanNamesEventMulticaster(listenerNames: Seq[String]) extends DefaultEventMulticaster with Initializing {

  var container: Container = _

  override def init() {
    listenerNames foreach { beanName =>
      if (container.contains(beanName)) addListener(container.getBean[EventListener[_]](beanName).get)
    }
  }
}
