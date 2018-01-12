package com.hwg.models

import com.hwg.models.Manuever.Manuever

import scala.collection.mutable.ArrayBuffer
import Manuever._


class Ship(var x: Double, var y: Double, var vX: Double, var vY: Double, var orientation: Double, var accelerating: Boolean = false, var manuevering: Manuever = Nothing, var firing: Boolean = false) {

  val projectiles: ArrayBuffer[Projectile] = ArrayBuffer()

  def updateCommands(commandShip: Ship): Unit = {
    accelerating = commandShip.accelerating
    manuevering = commandShip.manuevering
    firing = commandShip.firing
  }

  def tick(deltaTime: Double): Unit = {
    x = x + vX * deltaTime / 100
    y = y + vY * deltaTime / 100
    vX = if (accelerating) vX - 5 * Math.sin(orientation) * deltaTime / 100 else vX
    vY = if (accelerating) vY + 5 * Math.cos(orientation) * deltaTime / 100 else vY
    orientation = this.manuevering match {
      case CW => orientation - 0.1
      case CCW => orientation + 0.1
      case Reverse => orientation + 0.1
      case _ => orientation
    }
  }

}

