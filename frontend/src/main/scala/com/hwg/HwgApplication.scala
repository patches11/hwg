package com.hwg

import com.hwg.models.Ship
import com.hwg.util.{MatrixStack, Time}
import com.hwg.webgl.model.TwoDModel
import com.hwg.webgl.background.SolarSystem
import com.hwg.webgl.{Draw, DrawContext, HwgWebGLProgram, TextureLoader}
import monix.reactive.Observable
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.raw.{WebGLRenderingContext, WheelEvent}
import Protocol.{Dead, Initialized, State, ThisShip}
import com.hwg.gui.{Chat, Radar}

import scala.scalajs.js
import js.Dynamic.{global => g}
import scala.collection.mutable
import scala.scalajs.js

class HwgApplication(gl: WebGLRenderingContext, keyboardEvents: Observable[KeyboardEvent], wheelEvents: Observable[WheelEvent] ) {
  import WebGLRenderingContext._
  import com.hwg.models.ShipControls._
  import monix.execution.Scheduler.Implicits.global

  gl.pixelStorei(UNPACK_PREMULTIPLY_ALPHA_WEBGL, 1)

  val client = new WebsocketClient()
  val time = new Time(client)
  val chat = new Chat(client, 0)
  val textureLoader = new TextureLoader(gl)

  val thisShip: Ship = Ship()
  var id: Option[Int] = None

  thisShip.control(keyboardEvents)

  val ships: mutable.Map[Int, Ship] = mutable.Map()
  val matrixStack = new MatrixStack()

  val system = SolarSystem(gl)

  val shipModel = TwoDModel(textureLoader.get("/img/shipA.png"), gl, 66, 100)
  val laserModel = TwoDModel(textureLoader.get("/img/laserA.png"), gl, 25, 50)

  var lastTick: Long = time.now
  var tickInterval: Long = 25

  var lastReceivedState: Option[State] = None

  js.timers.setTimeout(tickInterval)(tick)

  val radar = new Radar()

  private val drawContext = new DrawContext()

  val draw: () => Unit = () => {
    val timeNow = time.now

    gl.viewport(0, 0, gl.canvas.width, gl.canvas.height)
    gl.clear(COLOR_BUFFER_BIT)

    system.draw(drawContext, thisShip, timeNow, program)

    ships.foreach { case (_, ship) =>
      drawContext { ms =>
        ms.translate(ship.x, ship.y, -20)
        ms.rotateZ(ship.orientation)

        shipModel.draw(program, matrixStack, thisShip.x, thisShip.y)
      } at -20

      ship.projectiles.foreach { projectile =>
        drawContext { ms =>
          ms.translate(projectile.x, projectile.y, -20)
          ms.rotateZ(projectile.orientation)

          laserModel.draw(program, matrixStack, thisShip.x, thisShip.y)
        } at -20
      }
    }

    drawContext.execute(matrixStack)

    radar.draw(id, thisShip, ships, system.planets)

    program.setCamera(thisShip.x, thisShip.y)
  }

  val program = HwgWebGLProgram(gl, draw)

  client.getObservable.collect {
    case Initialized(lId) =>
      id = Some(lId)
      ships.update(lId, thisShip)
    case s: State =>
      lastReceivedState = Some(s)
    case Dead(dId) =>
      if (id.contains(dId))
        g.alert("Dead!!!!!")
  }.subscribe()

  wheelEvents.foreach { we =>
    program.zoom(we.deltaY)
  }

  def tick(): Unit = {
    lastReceivedState.foreach { state =>
      lastTick = state.id
      //this.tickInterval = tick.tickInterval
      state.ships.foreach { case (id, shipInfo) =>
        val ship = ships.getOrElse(id, Ship())
        ship.updateFrom(shipInfo)
        ships(id) = ship
      }
      lastReceivedState = None
    }

    val thisTime = time.now
    val deltaTime = thisTime - lastTick

    ships.foreach { case (_, ship) =>
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
