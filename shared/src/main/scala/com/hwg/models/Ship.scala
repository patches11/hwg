package com.hwg.models

import com.hwg.models.Manuever.Manuever

import scala.collection.mutable.ArrayBuffer

case class Ship(x: Double, y: Double, vX: Double, vY: Double, orientation: Double, accelerating: Boolean, manuevering: Manuever, firing: Boolean) {
  import Manuever._

  val projectiles: ArrayBuffer[Projectile] = ArrayBuffer

  def updateCommands(commandShip: Ship): Ship = {
    this.copy(
      accelerating = commandShip.accelerating,
      manuevering = commandShip.manuevering,
      firing = commandShip.firing
    )
  }

  def tick(deltaTime: Double): Ship = {
    this.copy(
      x = this.x + this.vX * deltaTime / 100,
      y = this.y + this.vY * deltaTime / 100,
      vX = if (this.accelerating) this.vX - 5 * Math.sin(this.orientation) * deltaTime / 100 else this.vX,
      vY = if (this.accelerating) this.vY + 5 * Math.cos(this.orientation) * deltaTime / 100 else this.vY,
      orientation = this.manuevering match {
        case CW => this.orientation - 0.1
        case CCW => this.orientation - 0.1
        case Reverse => this.orientation - 0.1
      }
    )
  }

}

