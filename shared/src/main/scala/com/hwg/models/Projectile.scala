package com.hwg.models

case class Projectile(x: Double, y: Double, vX: Double, vY: Double, orientation: Double) {

  def tick(deltaTime: Double): Projectile = {
    this.copy(
      x = this.x + this.vX * deltaTime / 100,
      y = this.y + this.vY * deltaTime / 100
    )
  }
}
