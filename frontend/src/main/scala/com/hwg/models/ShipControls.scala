package com.hwg.models

import com.hwg.gui.Chat
import monix.reactive.Observable
import org.scalajs.dom.{KeyboardEvent, TouchEvent, TouchList}
import slogging.LazyLogging

object ShipControls extends LazyLogging {

  import monix.execution.Scheduler.Implicits.global
  import com.hwg.util.TouchListUtils._

  implicit class ShipControl(ship: Ship) {
    def handleTouches(list: TouchList, eventName: String): Unit = {
      val touches = list.getTouches()
      ship.accelerating = false
      ship.manuevering = Manuever.Nothing
      ship.firing = false
      touches.foreach { touch =>
        if (touch.xFrac >= 0.8 && touch.yFromBottom <= 200) {
          ship.firing = true
        }
        if (touch.xFrac <= 0.2) {
          if (touch.xFrac <= 0.075) {
            ship.manuevering = Manuever.CCW
          }
          if (touch.xFrac >= 0.125) {
            ship.manuevering = Manuever.CW
          }
          if (touch.xFrac > 0.075 && touch.xFrac < 0.125) {
            if (touch.yFromBottom <= 250 && touch.yFromBottom >= 175) {
              ship.accelerating = true
            } else {
              ship.manuevering = Manuever.Reverse
            }
          }
        }
      }
    }

    def control(obs: Observable[KeyboardEvent], touch: Observable[TouchEvent], chat: Chat): Unit = {
      obs.foreach {
        case ev if ev.key == "ArrowUp" =>
          if (ev.`type` == "keyup") {
            ship.accelerating = false
          } else {
            ship.accelerating = true
          }
        case ev if ev.key == "ArrowDown" =>
          if (ev.`type` == "keyup") {
            ship.manuevering = Manuever.Nothing
          } else {
            ship.manuevering = Manuever.Reverse
          }
        case ev if ev.key == "ArrowLeft" =>
          if (ev.`type` == "keyup") {
            ship.manuevering = Manuever.Nothing
          } else {
            ship.manuevering = Manuever.CCW
          }
        case ev if ev.key == "ArrowRight" =>
          if (ev.`type` == "keyup") {
            ship.manuevering = Manuever.Nothing
          } else {
            ship.manuevering = Manuever.CW
          }
        case ev if ev.keyCode == 32 => // Space
          if (ev.`type` == "keyup") {
            ship.firing = false
          } else {
            ship.firing = true
          }
        case _ => // Observables have a nice property of silently swallowing errors so you have to do this
      }
      touch.foreach(ev => handleTouches(ev.touches, ev.`type`))
    }
  }
}
