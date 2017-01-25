package org.beangle.commons.model

import java.time.LocalDate
import java.time.LocalDateTime

trait DateTimeRange {
  /**
   * 起始日期
   */
  var beginOn: LocalDateTime = _

  /**
   * 结束日期
   */
  var endOn: LocalDateTime = _
}
