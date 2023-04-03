package com.hwg.util

import org.scalajs.dom
import org.scalajs.dom.TouchList

case class Touch(x: Double, y: Double, xFromRight: Double, yFromBottom: Double, xFrac: Double, yFrac: Double)

object TouchListUtils {

  implicit class TouchListFuncs(list: TouchList) {
    def getTouches(): IndexedSeq[Touch] = {
      Range(0, list.length).map(i => {
        val localTouch = list.item(0)
        val x = localTouch.pageX
        val y = localTouch.pageY
        val xFromRight = dom.document.body.scrollWidth - x
        val yFromBottom = dom.document.body.scrollHeight - y
        val xFrac = x / dom.document.body.scrollWidth
        val yFrac = y / dom.document.body.scrollHeight
        Touch(x, y, xFromRight, yFromBottom, xFrac, yFrac)
      })
    }
  }
}
