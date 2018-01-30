package com.hwg.models

trait Entity {
  var x: Double
  var y: Double
  var speed: Double
  var speedOrientation: Double
  var orientation: Double

  def vX: Double = - Math.sin(speedOrientation) * speed
  def vY: Double = Math.cos(speedOrientation) * speed

  def tick(deltaTime: Double): Entity = {
    x = this.x + vX * deltaTime / 100
    y = this.y + vY * deltaTime / 100
    this
  }

  def updateFrom(locationEntity: Entity): Entity = {
    x = locationEntity.x
    y = locationEntity.y
    speed = locationEntity.speed
    speedOrientation = locationEntity.speedOrientation
    orientation = locationEntity.orientation
    this
  }
}