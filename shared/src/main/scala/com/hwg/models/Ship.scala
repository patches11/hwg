package com.hwg.models

import scala.collection.mutable.ArrayBuffer
import Manuever._


case class Ship(var x: Double = 0, var y: Double = 0, var vX: Double = 0, var vY: Double = 0, var orientation: Double = 0, var accelerating: Boolean = false, var manuevering: Manuever = Nothing, var firing: Boolean = false) {

  val projectiles: ArrayBuffer[Projectile] = ArrayBuffer()

  var firePause: Double = 0

  def updateCommands(commandShip: Ship): Ship = {
    accelerating = commandShip.accelerating
    manuevering = commandShip.manuevering
    firing = commandShip.firing
    this
  }

  def updateFrom(locationShip: Ship): Ship = {
    x = locationShip.x
    y = locationShip.y
    vX = locationShip.vX
    vY = locationShip.vY
    orientation = locationShip.orientation
    this
  }

  def tick(deltaTime: Double): Ship = {
    x = x + vX * deltaTime / 100
    y = y + vY * deltaTime / 100
    vX = if (accelerating) vX - 5 * Math.sin(orientation) * deltaTime / 100 else vX
    vY = if (accelerating) vY + 5 * Math.cos(orientation) * deltaTime / 100 else vY

    if (firing && firePause <= 0) {
      firePause = 1000
      projectiles.append(Projectile(
        x - 20 * Math.sin(orientation),
        y + 20 * Math.cos(orientation),
        - 100 * Math.sin(orientation),
        100 * Math.cos(orientation),
        orientation))
    } else {
      firePause -= deltaTime
    }

    orientation = this.manuevering match {
      case CW => orientation - 0.1
      case CCW => orientation + 0.1
      case Reverse => orientation + 0.1
      case _ => orientation
    }
    this
  }

}

