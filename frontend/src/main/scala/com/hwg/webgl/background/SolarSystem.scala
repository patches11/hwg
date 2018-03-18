package com.hwg.webgl.background

import com.hwg.background.{PlanetTexture, StarField, TextureOptions}
import com.hwg.models.Ship
import com.hwg.util.noise.PerlinNoise
import com.hwg.util.{Color, MatrixStack}
import com.hwg.webgl.model.{Model, SphereModel, TwoDModel}
import com.hwg.webgl.{HwgWebGLProgram, TextureInfo}
import org.scalajs.dom.raw.WebGLRenderingContext
import scryetek.vecmath.Vec3

import scala.scalajs.js
import scala.util.Random

case class SolarSystem(seed: Long, gl: WebGLRenderingContext) {

  import com.hwg.util.TypedArrayConverters._
  import com.hwg.util.VecmathConverters._

  val ambientColor = Float32Array(js.Array(0.5f, 0.5f, 0.5f))

  val lightDirection: Vec3 = Vec3(1, 1, -1)

  val directionalLightColor = Float32Array(js.Array(0.5f, 0.5f, 0.5f))

  // BG Config
  val backgroundSize: Int = 2048
  val pointsNear: Int = 10
  val minRadius: Int = 1
  val maxRadius: Int = 25 // TODO Why does this effect how this looks so much?
  val perlinMod: Int = 10
  val maxIterations: Int = 25000
  val random = new Random(this.seed)

  private val tex = TextureInfo.createFromUrl(gl, s"/img/planet_$seed.png")
  private val starFieldTex = TextureInfo.createFromUrl(gl, s"/img/background_$seed.png")

  val planets = Array {
    val planetModel = SphereModel(tex, gl, 8)
    Planet(planetModel, 10, 0, -30, 8)
  }

  val tex2d = TwoDModel(tex, gl, 800, 400)
  val starField: Model = TwoDModel(starFieldTex, gl, 25 * this.backgroundSize, 25 * this.backgroundSize)

  /*
  val planets: Array[Planet] = (0 until 3).map { _ =>
    val size = random.in(2, 5)
    val textureArray = PlanetTexture.generate(textureOptions)
    val tex = TextureInfo.createFromTex(gl, textureOptions.width, textureOptions.height, Uint8Array(textureArray))
    val planetModel = SphereModel(tex, gl, size)
    Planet(planetModel, random.in(-100, 100), random.in(-100, 100), random.in(-35, -30), size)
  }.toArray*/

  private val smokeTex = TextureInfo.createFromUrl(gl, "/img/smoke.png")
  private val moonTex = TextureInfo.createFromUrl(gl, "/img/moon.gif")
  private val smoke = Smoke(smokeTex, gl)

  def draw(matrixStack: MatrixStack, thisShip: Ship, time: Long, program: HwgWebGLProgram): Unit = {

    gl.uniform3fv(
      program.ambientColor,
      this.ambientColor
    )

    val adjustedLD = (new Vec3).set(lightDirection)
    adjustedLD.normalize()
    adjustedLD.scale(-1)

    gl.uniform3fv(program.lightingDir, adjustedLD.toJsArray)

    gl.uniform3fv(
      program.dirColor,
      this.directionalLightColor
    )

    matrixStack.save()
    matrixStack.translate(thisShip.x, thisShip.y, -100)
    starField.draw(program, matrixStack, thisShip.x, thisShip.y)
    matrixStack.restore()

    //smoke.draw(program, matrixStack, thisShip.x / 100, thisShip.y / 100, time)

    planets.foreach { planet =>
      matrixStack.save()
      matrixStack.translate(planet.x, planet.y, planet.z)
      matrixStack.rotateZ(Math.sin(time.toDouble / 10000 + Math.PI) * Math.PI)
      matrixStack.rotateX(Math.cos(time.toDouble / 10000 + Math.PI) * Math.PI)
      planet.model.draw(program, matrixStack, thisShip.x, thisShip.y)
      matrixStack.restore()
    }

    matrixStack.save()
    matrixStack.translate(-2, 0, -5)
    //tex2d.draw(program, matrixStack, thisShip.x, thisShip.y)
    matrixStack.restore()

  }
}

case class Planet(model: Model, x: Double, y: Double, z: Double, size: Double)
