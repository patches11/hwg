package com.hwg.models

import monix.reactive.Observable
import org.scalajs.dom.KeyboardEvent

object ShipControls {
  import monix.execution.Scheduler.Implicits.global

  implicit class ShipControl(ship: Ship) {
    def control(obs: Observable[KeyboardEvent]): Unit = {
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
    }
  }
}
