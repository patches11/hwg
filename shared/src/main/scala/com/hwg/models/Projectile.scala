package com.hwg.models

case class Projectile(var x: Double, var y: Double, var vX: Double, var vY: Double, var orientation: Double) {

  def tick(deltaTime: Double): Projectile = {
    x = this.x + this.vX * deltaTime / 100
    y = this.y + this.vY * deltaTime / 100
    this
  }
}
