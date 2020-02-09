package com.hwg.webgl.background

import com.hwg.models.Ship
import com.hwg.universe.Universe
import com.hwg.util.{MathExt, MatrixStack}
import com.hwg.webgl.model.{Model, SphereModel, TwoDModel}
import com.hwg.webgl.{Draw, DrawContext, HwgWebGLProgram, TextureInfo}
import org.scalajs.dom.raw.WebGLRenderingContext
import scryetek.vecmath.Vec3

import scala.scalajs.js

case class SolarSystem(gl: WebGLRenderingContext) {

  import com.hwg.util.TypedArrayConverters._
  import com.hwg.util.VecmathConverters._

  val ambientColor = Float32Array(js.Array(0.1f, 0.2f, 0.3f))

  val lightDirection: Vec3 = Vec3(1, 1, -1)

  val directionalLightColor = Float32Array(js.Array(0.0f, 0.4f, 0.2f))

  // BG Config
  val backgroundSize: Int = 4092
  val foregroundSize: Int = 3072 // Get this from somewhere else
  val foregroundMod: Int = 20
  val backgroundMod: Int = 10

  val system = Universe.systems.head

  println(s"system: ${system.seed}")

  private val bg = TextureInfo.createFromUrl(gl, system.background)
  private val fg = TextureInfo.createFromUrl(gl, system.foreground)
  val starField: Model = TwoDModel(bg, gl, backgroundMod * this.backgroundSize, backgroundMod * this.backgroundSize)
  val haze: Model = TwoDModel(fg, gl, foregroundMod * foregroundSize, foregroundMod * foregroundSize)


  val planets: Array[Planet] = system.planets.zipWithIndex.map { case (planet, i) =>
    val size = planet.size
    val tex = TextureInfo.createFromUrl(gl, system.tex(i))
    val planetModel = SphereModel(tex, gl, size)
    Planet(planetModel, planet.center._1, planet.center._2, -35, planet.size)
  }.toArray

  private val smokeTex = TextureInfo.createFromUrl(gl, "/img/smoke.png")
  private val smoke = Smoke(smokeTex, gl, SmokeOptions(center = (100, 100, -40)))

  private val pointSquare = MathExt.genPointSquare(3, includeOrigin = true)

  def draw(draw: DrawContext, thisShip: Ship, time: Long, program: HwgWebGLProgram): Unit = {

    val boxX = (thisShip.x / (foregroundSize / (400 / foregroundMod))).toInt
    val boxY = (thisShip.y / (foregroundSize / (400 / foregroundMod))).toInt
    val foregroundAdj = foregroundSize / (200 / foregroundMod)

    draw { _ =>
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
    } at 0

    draw { ms =>
      ms.translate(thisShip.x, thisShip.y, -50)
      starField.draw(program, ms, thisShip.x, thisShip.y)
    } at -50

    draw ++= smoke.draw(program, thisShip.x / 100, thisShip.y / 100, time)

    planets.foreach { planet =>
      draw { ms =>
        ms.translate(planet.x, planet.y, planet.z)
        ms.rotateZ(Math.sin(time.toDouble / 1000000 + Math.PI) * Math.PI)
        ms.rotateX(Math.cos(time.toDouble / 1000000 + Math.PI) * Math.PI)
        planet.model.draw(program, ms, thisShip.x, thisShip.y)
      } at planet.z
    }

    pointSquare.foreach { point =>
      draw { ms =>
        ms.translate((boxX + point.x) * foregroundAdj, (boxY + point.y) * foregroundAdj, -15)
        haze.draw(program, ms, thisShip.x, thisShip.y)
      } at -15
    }

  }
}

case class Planet(model: Model, x: Double, y: Double, z: Double, size: Double)
