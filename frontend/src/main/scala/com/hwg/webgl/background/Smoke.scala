package com.hwg.webgl.background

import com.hwg.util.MatrixStack
import com.hwg.webgl.model.TwoDModel
import com.hwg.webgl.{Draw, HwgWebGLProgram, TextureInfo}
import org.scalajs.dom.raw.WebGLRenderingContext

import scala.scalajs.js
import scala.util.Random

case class Smoke(texture: TextureInfo, gl: WebGLRenderingContext, options: SmokeOptions = SmokeOptions()) {

  val tint = js.Array(options.tint._1, options.tint._2, options.tint._3, options.tint._4)

  private val internalModel = TwoDModel(texture, gl, (400 * options.scale).toInt, (400 * options.scale).toInt)

  private val locations = (0 until options.elements).map { _ =>
    (
      options.center._1 - options.spread._1 / 2 + Random.nextDouble() * options.spread._1,
      options.center._2 - options.spread._2 / 2 + Random.nextDouble() * options.spread._2,
      options.center._3 - options.spread._3 / 2 + Random.nextDouble() * options.spread._3,
      Random.nextDouble() * 2 * Math.PI)
  }.sortBy(_._3)

  def draw(program: HwgWebGLProgram, cameraX: Double, cameraY: Double, time: Long): Array[Draw] = {
    locations.map { case (sx, sy, sz, sRot) =>
      Draw(sz, (ms: MatrixStack) => {
        ms.translate(sx, sy, sz)
        ms.rotateZ(sRot + Math.sin(time.toDouble / 100000 + Math.PI) * Math.PI)
        internalModel.draw(program, ms, cameraX, cameraY, tint)
      })
    }.toArray
  }
}

case class SmokeOptions(
                        tint: (Double, Double, Double, Double) = (3.0, 0.1, 0.1, 0.1),
                        scale: Double = 20,
                        spread: (Double, Double, Double) = (30, 30, 4),
                        center: (Double, Double, Double) = (0, 0, -40),
                        elements: Int = 5
                       )