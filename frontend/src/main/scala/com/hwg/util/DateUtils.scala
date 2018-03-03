package com.hwg.util

import scala.scalajs.js.Date

object DateUtils {

  implicit class DateFuncs(date: Date) {
    def twelveHours(): Int = {
      val d = date.getHours() % 12
      if (d == 0) {
        12
      } else {
        d
      }
    }

    def timeFormat(): String = {
      val m = date.getMinutes.toString
      s"${date.twelveHours()}:${if (m.length < 2) "0" + m else m}"
    }
  }
}
