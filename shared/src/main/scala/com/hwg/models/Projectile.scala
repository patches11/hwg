package com.hwg.models

import scala.concurrent.duration._

case class Projectile(var x: Double, var y: Double, var speed: Double, var speedOrientation: Double, var orientation: Double) extends Entity {
  var lifetime = 2 seconds

  override def tick(deltaTime: Double): Entity = {
    super.tick(deltaTime)
    lifetime = lifetime.minus(deltaTime millis)
    this
  }
}