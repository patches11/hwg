package com.hwg.gui

import com.hwg.models.Ship
import com.hwg.util.MathExt
import com.hwg.webgl.background.Planet
import org.scalajs.dom
import org.scalajs.dom.{html, raw}

import scala.collection.mutable
import scala.scalajs.js.Array

class Radar() {

  private val radar = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
  private val context = radar.getContext("2d").asInstanceOf[raw.CanvasRenderingContext2D]

  val uiScale = 1.0
  val size: Int = (200 * uiScale).toInt
  val margin = 20
  radar.style.position = "fixed"
  radar.width = size
  radar.height = size
  radar.style.width = s"${size}px"
  radar.style.height = s"${size}px"
  radar.style.right = s"${margin}px"
  radar.style.top = s"${margin}px"
  val shipSize = (uiScale * 2).toInt

  dom.document.body.appendChild(radar)
  val range = 50
  val outlineWidth = uiScale * 4


  def draw(idOption: Option[Int], thisShip: Ship, ships: mutable.Map[Int, Ship], planets: Array[Planet]): Unit = {
    context.clearRect(0, 0, size, size)

    // Base
    context.fillStyle = Colors.baseGray
    context.strokeStyle = Colors.outlineGray
    context.lineWidth = uiScale * 10
    context.beginPath()
    context.arc(size / 2, size / 2, size / 2 - outlineWidth, 0, Math.PI * 2)
    context.stroke()
    context.fill()

    context.lineWidth = uiScale

    idOption.foreach { id =>
      // You
      context.fillStyle = Colors.blue
      context.fillRect(size / 2 - shipSize / 2, size / 2 - shipSize / 2, shipSize, shipSize)

      // Planets
      context.strokeStyle = Colors.green
      planets.foreach { planet =>
        val distance = MathExt.distance(thisShip.x, thisShip.y, planet.x, planet.y) / range
        if (distance < size / 2 - planet.size * 2) {
          context.lineWidth = uiScale
          context.beginPath()
          context.arc(size / 2 - (thisShip.x - planet.x) / range, size / 2 + (thisShip.y - planet.y) / range, planet.size * 2, 0, Math.PI * 2)
          context.stroke()
        } else {
          context.lineWidth = uiScale * 2
          val angle = MathExt.arctan(thisShip.x - planet.x, thisShip.y - planet.y)
          context.beginPath()
          context.moveTo(size / 2 - Math.cos(angle) * (size / 2 + 1), size / 2 + Math.sin(angle) * (size / 2 + 1))
          context.lineTo(size / 2 - Math.cos(angle) * (size / 2 - uiScale * 6), size / 2 + Math.sin(angle) * (size / 2 - uiScale * 6))
          context.stroke()
        }
      }

      // Others
      context.fillStyle = Colors.red
      context.strokeStyle = Colors.red
      ships.foreach { case (sid, ship) =>
        if (id != sid) {
          val distance = MathExt.distance(thisShip.x, thisShip.y, ship.x, ship.y) / range
          context.fillRect(size / 2 - shipSize / 2 - (thisShip.x - ship.x) / range, size / 2 - shipSize / 2 + (thisShip.y - ship.y) / range, shipSize, shipSize)
          // TODO : Finish
        }
      }
    }
  }

  case object Colors {
    val blue = "#2F83C3"
    val green = "#7FA15B"
    val red = "#E94E3D"

    val baseGray = "#202022"
    val outlineGray = "#404041"
  }


}
