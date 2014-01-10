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
package org.beangle.commons.web.session

import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener
import org.beangle.commons.event.EventMulticaster
import org.beangle.commons.inject.Containers
import org.beangle.commons.lang.Assert
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Declared in web.xml as
 *
 * <pre>
 * &lt;listener&gt;
 *     &lt;listener-class&gt;org.beangle.commons.web.session.HttpSessionEventPublisher&lt;/listener-class&gt;
 * &lt;/listener&gt;
 * </pre>
 *
 * Publishes <code>HttpSessionApplicationEvent</code>s to the Bean Root Context.
 * Maps javax.servlet.http.HttpSessionListener.sessionCreated() to {@link HttpSessionCreationEvent}.
 * Maps javax.servlet.http.HttpSessionListener.sessionDestroyed() to
 * {@link HttpSessionDestroyedEvent}.
 */
class HttpSessionEventPublisher extends HttpSessionListener {

  protected var eventMulticaster: EventMulticaster = _

  /**
   * Handles the HttpSessionEvent by publishing a {@link HttpSessionCreationEvent} to the
   * application appContext.
   *
   * @param event HttpSessionEvent passed in by the container
   */
  def sessionCreated(event: HttpSessionEvent) {
    if (null == eventMulticaster) {
      eventMulticaster = Containers.getRoot.getBean(classOf[EventMulticaster])
        .get
      eventMulticaster
    }
    eventMulticaster.multicast(new HttpSessionCreationEvent(event.getSession))
  }

  /**
   * Handles the HttpSessionEvent by publishing a {@link HttpSessionDestroyedEvent} to the
   * application appContext.
   *
   * @param event The HttpSessionEvent pass in by the container
   */
  def sessionDestroyed(event: HttpSessionEvent) {
    if (null != eventMulticaster) {
      eventMulticaster.multicast(new HttpSessionDestroyedEvent(event.getSession))
    }
  }
}
