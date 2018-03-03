package com.hwg.models

import scala.collection.mutable.ArrayBuffer
import Manuever._
import com.hwg.util.MathExt


case class Ship(var x: Double = 0, var y: Double = 0, var speed: Double = 0,
                var speedOrientation: Double = 0, var orientation: Double = 0,
                var accelerating: Boolean = false, var manuevering: Manuever = Nothing, var firing: Boolean = false,
                var projectiles: ArrayBuffer[Projectile] = ArrayBuffer()) extends Entity {

  var firePause: Double = 0
  val firePauseTime: Int = 100

  val speedMax = 50
  val oDiff = 0.075

  def updateCommands(commandShip: Ship): Ship = {
    accelerating = commandShip.accelerating
    manuevering = commandShip.manuevering
    firing = commandShip.firing
    this
  }

  override def tick(deltaTime: Double): Ship = {
    super.tick(deltaTime)

    if (accelerating) {
      val nVX = vX - 5 * Math.sin(orientation) * deltaTime / 100
      val nVY = vY + 5 * Math.cos(orientation) * deltaTime / 100

      speed = Math.min(Math.sqrt(nVX * nVX + nVY * nVY), speedMax)
      speedOrientation = MathExt.arctan(nVY, -nVX)
    }

    if (firing && firePause <= 0) {
      firePause = firePauseTime
      projectiles.append(Projectile(
        x - 20 * Math.sin(orientation),
        y + 20 * Math.cos(orientation),
        100,
        orientation,
        orientation))
    } else {
      firePause -= deltaTime
    }

    orientation = this.manuevering match {
      case CW => orientation - oDiff
      case CCW => orientation + oDiff
      case Reverse =>
        val diff = (orientation - speedOrientation + MathExt.TwoPi) % MathExt.TwoPi
        orientation + (if (diff < Math.PI) oDiff else -oDiff)
      case _ => orientation
    }
    this
  }

  def updateFrom(locationEntity: Ship): Ship = {
    super.updateFrom(locationEntity)
    projectiles = locationEntity.projectiles
    this
  }
}

