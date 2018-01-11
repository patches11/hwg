package com.hwg.webgl.background

import com.hwg.background.StarField
import com.hwg.models.Ship
import com.hwg.util.{MatrixStack, PerlinNoise}
import com.hwg.webgl.{HwgWebGLProgram, TextureInfo}
import com.hwg.webgl.model.{Model, SphereModel, TwoDModel}
import org.scalajs.dom.raw.WebGLRenderingContext
import scryetek.vecmath.Vec3

import scala.util.Random

case class SolarSystem(seed: Long, gl: WebGLRenderingContext) {
  import com.hwg.util.VecmathConverters._
  import com.hwg.util.TypedArrayConverters._

  val ambientColor = Float32Array(Array(0.1f, 0.5f, 0.7f))

  val lightDirection: (Float, Float, Float) = (1, 1, -1)

  val directionalLightColor = Float32Array(Array(0.5f, 0.5f, 0f))

  // BG Config
  val backgroundSize: Int = 2048
  val pointsNear: Int = 10
  val minRadius: Int = 1
  val maxRadius: Int = 25 // TODO Why does this effect how this looks so much?
  val perlinMod: Int = 10
  val maxIterations: Int = 25000

  private val moon = TextureInfo.createFromUrl(gl, "/img/moon.gif")

  private val starData = StarField.generateTexture(this.backgroundSize, this.backgroundSize, this.pointsNear, this.minRadius, this.maxRadius,
    (x: Double, y: Double) => { PerlinNoise.noise(x / this.backgroundSize * this.perlinMod, y / this.backgroundSize * this.perlinMod,
      this.seed)}, new Random(this.seed), this.maxIterations)

  private val starFieldTex = TextureInfo.createFromTex(gl, backgroundSize, backgroundSize, Uint8Array(starData))

  val moonModel = SphereModel(moon, gl)

  val planets: Array[Planet]= Array(Planet(moonModel, 0, 0))
  val starField: Model = TwoDModel(starFieldTex, gl, 25*this.backgroundSize, 25*this.backgroundSize)

  def draw(matrixStack: MatrixStack, thisShip: Ship, time: Long, program: HwgWebGLProgram): Unit = {

    gl.uniform3fv(
      program.ambientColor,
      this.ambientColor
    )

    val adjustedLD = Vec3(lightDirection._1, lightDirection._2, lightDirection._3)
    adjustedLD.normalize()
    adjustedLD.scale(-1)

    gl.uniform3fv(program.lightingDir, adjustedLD.toJsArray)

    gl.uniform3fv(
      program.dirColor,
      this.directionalLightColor
    )

    //matrixStack.save()
    //matrixStack.translate(thisShip.x / 100, thisShip.y / 100, -100)
    //starField.draw(program, matrixStack, thisShip.x / 100, thisShip.y / 100)
    //matrixStack.restore()

    planets.foreach { planet =>
      matrixStack.save()
      matrixStack.translate(planet.x, planet.y, -16)
      matrixStack.rotateZ(Math.sin(time / 1000000 + Math.PI) * Math.PI)
      matrixStack.rotateX(Math.cos(time / 1000000 + Math.PI) * Math.PI)
      planet.model.draw(program, matrixStack, thisShip.x / 100, thisShip.y / 100)
      matrixStack.restore()
    }

  }
}

case class Planet(model: Model, x: Long, y: Long)
