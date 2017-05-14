package org.beangle.commons.model.pojo

import java.time.LocalDateTime

trait DateTimeRange {
  /**
   * 起始日期
   */
  var beginAt: LocalDateTime = _

  /**
   * 结束日期
   */
  var endAt: LocalDateTime = _
}
