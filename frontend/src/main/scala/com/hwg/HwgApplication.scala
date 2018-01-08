package com.hwg

import com.hwg.models.Ship
import com.hwg.util.Time
import com.hwg.webgl.{HwgWebGLProgram, TextureLoader}
import org.scalajs.dom.raw.WebGLRenderingContext

import scala.collection.mutable.ArrayBuffer

class HwgApplication(gl: WebGLRenderingContext) {
  val GL = WebGLRenderingContext

  val client = new WebsocketClient()
  val time = new Time(client)
  val textureLoader = new TextureLoader(gl)
  val ships: ArrayBuffer[Ship] = ArrayBuffer()
  val thisShip: Ship

  def draw(): Unit = {
    val timeNow = time.now

    gl.viewport(0, 0, gl.canvas.width, gl.canvas.height);
    gl.clear(GL.COLOR_BUFFER_BIT);

    system.draw(matrixStack, thisShip, time)

    ships.foreach { ship =>

      matrixStack.save()
      matrixStack.translate(ship.x / 100, ship.y / 100, -10)
      matrixStack.rotateZ(ship.orientation)

      shipModel.draw(matrixStack, application.thisShip.x / 100, application.thisShip.y / 100)

      matrixStack.restore()

      ship.projectiles.foreach { projectile =>
        matrixStack.save()
        matrixStack.translate(projectile.x / 100, projectile.y / 100, -10);
        matrixStack.rotateZ(projectile.orientation)

        laserModel.draw(matrixStack, application.thisShip.x / 100, application.thisShip.y / 100)

        matrixStack.restore();
      }
    }
  }

  val program = HwgWebGLProgram(gl, draw)
}
