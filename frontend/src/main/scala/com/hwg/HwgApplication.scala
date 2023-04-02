package com.hwg

import com.hwg.models.{Entity, Ship}
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

  val updateEntity = (entity: Entity, model: TwoDModel, thisX: Double, thisY: Double) => {
    drawContext { ms =>
      ms.translate(entity.x, entity.y, -20)
      ms.rotateZ(entity.orientation)

      model.draw(program, matrixStack, thisX, thisY)
    } at -20
  }

  val draw: () => Unit = () => {
    val timeNow = time.now

    val thisX = thisShip.x
    val thisY = thisShip.y

    gl.viewport(0, 0, gl.canvas.width, gl.canvas.height)
    gl.clear(COLOR_BUFFER_BIT)

    program.setCamera(thisX, thisY)

    system.draw(drawContext, thisX, thisY, timeNow, program)

    ships.foreach { case (id, ship) =>
      updateEntity(ship, shipModel, thisX, thisY)

      ship.projectiles.foreach { projectile =>
        updateEntity(projectile, laserModel, thisX, thisY)
      }
    }

    drawContext.execute(matrixStack)

    radar.draw(id, thisShip, ships, system.planets)


    lastDraw = timeNow
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
    val deltaTime = lastReceivedState match {
      case Some((state, estimatedSentAt)) =>
        state.ships.foreach { case (id, shipInfo) =>
          val ship = ships.getOrElse(id, Ship())
          ship.updateFrom(shipInfo)
          ships(id) = ship
        }
        lastReceivedState = None
        time.nowRaw - estimatedSentAt
      case None =>
        time.nowRaw - lastTick
    }

    ships.foreach { case (_, ship) =>
      ship.tick(deltaTime)

      ship.projectiles.foreach(p => p.tick(deltaTime))
    }

    if (client.alive) {
      client.send(ThisShip(thisShip))
    }
    lastTick = time.nowRaw
    js.timers.RawTimers.setTimeout(tick, tickInterval)
  }

  js.timers.RawTimers.setTimeout(tick, tickInterval)
}
