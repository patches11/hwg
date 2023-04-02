package com.hwg

import com.hwg.models.Ship
import com.hwg.util.{MatrixStack, Time}
import com.hwg.webgl.model.TwoDModel
import com.hwg.webgl.background.SolarSystem
import com.hwg.webgl.{DrawContext, HwgWebGLProgram, TextureLoader}
import monix.reactive.Observable
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.raw.{WebGLRenderingContext, WheelEvent}
import Protocol.{Dead, Initialized, State, ThisShip}
import com.hwg.gui.{Chat, Radar}
import slogging.LazyLogging

import scala.scalajs.js
import js.Dynamic.{global => g}
import scala.collection.mutable

class HwgApplication(gl: WebGLRenderingContext, keyboardEvents: Observable[KeyboardEvent], wheelEvents: Observable[WheelEvent]) extends LazyLogging {

  import WebGLRenderingContext._
  import com.hwg.models.ShipControls._
  import monix.execution.Scheduler.Implicits.global

  gl.pixelStorei(UNPACK_PREMULTIPLY_ALPHA_WEBGL, 1)

  val client = new WebsocketClient()
  val time = new Time(client)
  new Chat(client, 0)
  private val textureLoader = new TextureLoader(gl)

  val thisShip: Ship = Ship()
  var id: Option[Int] = None

  thisShip.control(keyboardEvents)

  val ships: mutable.Map[Int, Ship] = mutable.Map()
  val matrixStack = new MatrixStack()

  private val system = SolarSystem(gl)

  private val shipModel = TwoDModel(textureLoader.get("/img/shipA.png"), gl, 66, 100)
  private val laserModel = TwoDModel(textureLoader.get("/img/laserA.png"), gl, 25, 50)

  private var lastTick: Long = time.now
  private val tickInterval: Long = 25
  private var lastDraw: Long = time.now

  private var lastReceivedState: Option[(State, Long)] = None


  val radar = new Radar()

  private val drawContext = new DrawContext()

  val draw: () => Unit = () => {
    logger.info("draw start")
    val timeNow = time.now

    val thisX = math.round(thisShip.x).toDouble
    val thisY = math.round(thisShip.y).toDouble

    gl.viewport(0, 0, gl.canvas.width, gl.canvas.height)
    gl.clear(COLOR_BUFFER_BIT)

    system.draw(drawContext, thisX, thisY, timeNow, program)

    logger.info(s"thisShip (${thisX}, ${thisY})")

    ships.foreach { case (id, ship) =>

      logger.info(s"ship $id (${ship.x}, ${ship.y})")
      drawContext { ms =>
        ms.translate(ship.x, ship.y, -20)
        ms.rotateZ(ship.orientation)

        shipModel.draw(program, matrixStack, thisX, thisY)
      } at -20

      ship.projectiles.foreach { projectile =>
        drawContext { ms =>
          ms.translate(projectile.x, projectile.y, -20)
          ms.rotateZ(projectile.orientation)

          laserModel.draw(program, matrixStack, thisX, thisY)
        } at -20
      }
    }

    drawContext.execute(matrixStack)

    radar.draw(id, thisShip, ships, system.planets)

    program.setCamera(thisX, thisY)

    lastDraw = timeNow
    logger.info("draw end")

  }

  private val program = HwgWebGLProgram(gl, draw)

  client.getObservable.collect {
    case Initialized(lId, version) =>
      id = Some(lId)
      ships.update(lId, thisShip)
      logger.info(s"Starting HWG $version")
    case s: State =>
      lastReceivedState = Some((s, time.estimatedSendTime))
    case Dead(dId) =>
      if (id.contains(dId))
        g.alert("Dead!!!!!")
  }.subscribe()

  wheelEvents.foreach { we =>
    program.zoom(we.deltaY)
  }

  private val tick: () => Unit = () => {
    logger.info("tick start")
    val deltaTime = lastReceivedState match {
      case Some((state, estimatedSentAt)) =>
        val d1 = estimatedSentAt - lastTick
        state.ships.foreach { case (id, shipInfo) =>
          val estShip = ships.getOrElse(id, Ship())
          estShip.tick(d1)
          logger.info(s"delta ${d1} estimated (${estShip.x}, ${estShip.y}) actual ${shipInfo.x}, ${shipInfo.y}")
        }
        state.ships.foreach { case (id, shipInfo) =>
          val ship = ships.getOrElse(id, Ship())
          ship.updateFrom(shipInfo)
          ships(id) = ship
        }
        lastReceivedState = None
        logger.info("update")
        time.nowRaw - estimatedSentAt
      case None =>
        logger.info("non-update")
        time.nowRaw - lastTick
    }

    logger.info(s"latency ${time.getLatency} delta time $deltaTime")

    ships.foreach { case (_, ship) =>
      ship.tick(deltaTime)

      ship.projectiles.foreach(p => p.tick(deltaTime))
    }

    if (client.alive) {
      client.send(ThisShip(thisShip))
    }
    lastTick = time.nowRaw
    logger.info("tick end")
    js.timers.RawTimers.setTimeout(tick, tickInterval)
  }

  js.timers.RawTimers.setTimeout(tick, tickInterval)
}
