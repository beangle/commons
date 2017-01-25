package org.beangle.commons.model

import java.time.LocalDate

trait DateRange {
  /**
   * 起始日期
   */
  var beginOn: LocalDate = _

  /**
   * 结束日期
   */
  var endOn: LocalDate = _
}
