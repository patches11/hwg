package com.hwg

import com.hwg.models.Ship
import com.hwg.util.{MatrixStack, Time}
import com.hwg.webgl.model.TwoDModel
import com.hwg.webgl.background.SolarSystem
import com.hwg.webgl.{HwgWebGLProgram, TextureLoader}
import org.scalajs.dom.raw.WebGLRenderingContext

import scala.scalajs.js

class HwgApplication(gl: WebGLRenderingContext) {
  import WebGLRenderingContext._

  val client = new WebsocketClient()
  val time = new Time(client)
  val textureLoader = new TextureLoader(gl)
  val thisShip: Ship = Ship(0, 0, 0, 0, 0)
  val ships: js.Array[Ship] = js.Array(thisShip)
  val matrixStack = new MatrixStack()

  val system = SolarSystem(31687, gl)

  val shipModel = TwoDModel(textureLoader.get("/img/ShipA.png"), gl, 50, 75)
  val laserModel = TwoDModel(textureLoader.get("/img/LaserA.png"), gl, 25, 50)

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
}
