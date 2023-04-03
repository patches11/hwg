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
          if (touch.xFrac <= 0.75) {
            ship.manuevering = Manuever.CCW
          }
          if (touch.xFrac >= 1.25) {
            ship.manuevering = Manuever.CW
          }
          if (touch.xFrac > 0.75 && touch.xFrac < 1.25) {
            if (touch.yFromBottom <= 200 && touch.yFromBottom >= 100) {
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
