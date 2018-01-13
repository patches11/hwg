package com.hwg

import com.hwg.models.Ship
import com.hwg.util.{MatrixStack, Time}
import com.hwg.webgl.model.TwoDModel
import com.hwg.webgl.background.SolarSystem
import com.hwg.webgl.{HwgWebGLProgram, TextureLoader}
import monix.reactive.Observable
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.raw.WebGLRenderingContext
import shared.Protocol.{State, ThisShip}

import scala.scalajs.js

class HwgApplication(gl: WebGLRenderingContext, keyboardEvents: Observable[KeyboardEvent] ) {
  import WebGLRenderingContext._
  import com.hwg.models.ShipControls._

  val client = new WebsocketClient()
  val time = new Time(client)
  val textureLoader = new TextureLoader(gl)
  val thisShip: Ship = new Ship(0, 0, 0, 0, 0)
  thisShip.control(keyboardEvents)

  val ships: js.Array[Ship] = js.Array(thisShip)
  val matrixStack = new MatrixStack()

  val system = SolarSystem(31687, gl)

  val shipModel = TwoDModel(textureLoader.get("/img/ShipA.png"), gl, 50, 75)
  val laserModel = TwoDModel(textureLoader.get("/img/LaserA.png"), gl, 25, 50)

  var lastTick: Long = time.now
  var tickInterval: Long = 25

  var lastReceivedState: Option[State] = None

  js.timers.setTimeout(tickInterval)(tick)

  val draw: () => Unit = () => {
    val timeNow = time.now

    gl.viewport(0, 0, gl.canvas.width, gl.canvas.height)
    gl.clear(COLOR_BUFFER_BIT)

    system.draw(matrixStack, thisShip, timeNow, program)

    ships.foreach { ship =>

      matrixStack.save()
      matrixStack.translate(ship.x / 100, ship.y / 100, -10)
      matrixStack.rotateZ(ship.orientation)

      shipModel.draw(program, matrixStack, thisShip.x / 100, thisShip.y / 100)

      matrixStack.restore()

      ship.projectiles.foreach { projectile =>
        matrixStack.save()
        matrixStack.translate(projectile.x / 100, projectile.y / 100, -10)
        matrixStack.rotateZ(projectile.orientation)

        laserModel.draw(program, matrixStack, thisShip.x / 100, thisShip.y / 100)

        matrixStack.restore();
      }
    }
  }

  val program = HwgWebGLProgram(gl, draw)

  def tick(): Unit = {
    lastReceivedState.foreach { state =>
      lastTick = state.id
      //this.tickInterval = tick.tickInterval
      state.ships.foreach { case (id, shipInfo) =>
        //val ship = this.ships[id] || new Ship()
        //ship.updateFrom(shipInfo)
        //this.ships[id] = ship
      }
      lastReceivedState = None
    }

    val thisTime = time.now
    val deltaTime = thisTime - lastTick

    ships.foreach { ship =>
      ship.tick(deltaTime)

      ship.projectiles.foreach((p) => p.tick(deltaTime))
    }

    if (client.alive) {
      client.send(ThisShip(thisShip))
    }

    val thisTickAhead = Math.floor((time.now - lastTick) / tickInterval) // Number of ticks ahead of last we are
    val target = thisTickAhead + 1 * tickInterval + lastTick

    lastTick = thisTime
    js.timers.setTimeout(target - time.now)(tick)
  }



}
