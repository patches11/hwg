package com.hwg.webgl

import org.scalajs.dom
import org.scalajs.dom.raw.{WebGLProgram, WebGLRenderingContext, WebGLShader, WebGLUniformLocation}
import scryetek.vecmath.Mat4
import slogging.LazyLogging

case class HwgWebGLProgram(gl: WebGLRenderingContext, draw: () => Unit) extends LazyLogging {

  import WebGLRenderingContext._
  import com.hwg.util.VecmathConverters._

  val program: WebGLProgram = initShaderProgram(Shaders.vShader, Shaders.fShader)
  var z: Double = 0

  gl.useProgram(program)

  val positionLocation: Int = gl.getAttribLocation(program, "aVertexPosition")
  gl.enableVertexAttribArray(positionLocation)
  val texcoordLocation: Int = gl.getAttribLocation(program, "aTextureCoord")
  gl.enableVertexAttribArray(texcoordLocation)
  val normalLocation: Int = gl.getAttribLocation(program, "aVertexNormal")
  gl.enableVertexAttribArray(normalLocation)

  val perspectiveMatrix: WebGLUniformLocation = gl.getUniformLocation(program, "uProjectionMatrix")
  val moveMatrix: WebGLUniformLocation = gl.getUniformLocation(program, "uModelViewMatrix")
  val normalMatrix: WebGLUniformLocation = gl.getUniformLocation(program, "uNormalMatrix")

  val ambientColor: WebGLUniformLocation = gl.getUniformLocation(program, "uAmbientColor")
  val lightingDir: WebGLUniformLocation = gl.getUniformLocation(program, "uLightingDirection")
  val dirColor: WebGLUniformLocation = gl.getUniformLocation(program, "uDirectionalColor")

  val samplerUniform: WebGLUniformLocation = gl.getUniformLocation(program, "uSampler")

  val colorUniform: WebGLUniformLocation = gl.getUniformLocation(program, "uColor")

  // Fill BG
  gl.clearColor(0.0, 0.0, 0.0, 1.0)

  // Enable Alpha
  gl.blendFunc(ONE, ONE_MINUS_SRC_ALPHA)
  gl.enable(BLEND)

  gl.enable(DEPTH_TEST) // Enable depth testing
  gl.depthFunc(LESS) // Near things obscure far things

  gl.clear(COLOR_BUFFER_BIT | DEPTH_BUFFER_BIT)
  gl.pixelStorei(UNPACK_PREMULTIPLY_ALPHA_WEBGL, 1)

  private val updater: scalajs.js.Function1[Double, _] = {
    (_: Double) => {
      resizeCanvasToDisplaySize()
      draw()
      dom.window.requestAnimationFrame(updater)
    }
  }

  dom.window.requestAnimationFrame(updater)

  def zoom(zDiff: Double): Unit = {
    z -= zDiff / 50
    if (z < -10) {
      z = -10
    } else if ( z > 10) {
      z = 10
    }
  }

  def setMatrixUniforms(mvMatrix: Mat4, x: Double, y: Double): Unit = {
    gl.uniformMatrix4fv(moveMatrix, transpose = false, mvMatrix.toJsArrayT)

    val normalMatrix = mvMatrix.dup
    normalMatrix.invert()
    normalMatrix.transpose()

    gl.uniformMatrix4fv(this.normalMatrix, transpose = false, normalMatrix.toJsArrayT)
  }

  def setCamera(x: Double, y: Double): Unit = {
    val cameraMatrix = Mat4().postTranslate(x.toFloat, y.toFloat, z.toFloat)
    val viewMatrix = cameraMatrix.dup
    viewMatrix.invert()

    val pMatrix = Mat4.perspective((Math.PI / 4).toFloat, gl.canvas.width.toFloat / gl.canvas.height, 0.1f, 200.0f)

    val pvMatrix = pMatrix.postMultiply(viewMatrix)

    gl.uniformMatrix4fv(this.perspectiveMatrix, transpose = false, pvMatrix.toJsArrayT)
  }

  private def initShaderProgram(vsSource: String, fsSource: String): WebGLProgram = {
    val vertexShader = loadShader(WebGLRenderingContext.VERTEX_SHADER, vsSource)
    val fragmentShader = loadShader(WebGLRenderingContext.FRAGMENT_SHADER, fsSource)

    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertexShader)
    gl.attachShader(shaderProgram, fragmentShader)
    gl.linkProgram(shaderProgram)

    if (!gl.getProgramParameter(shaderProgram, WebGLRenderingContext.LINK_STATUS).asInstanceOf[Boolean]) {
      logger.error("Unable to initialize the shader program: " + gl.getProgramInfoLog(shaderProgram))
      // TODO Throw Error
    }

    shaderProgram
  }

  private def loadShader(shaderType: Int, source: String): WebGLShader = {
    val shader = gl.createShader(shaderType)

    gl.shaderSource(shader, source)

    gl.compileShader(shader)

    if (!gl.getShaderParameter(shader, WebGLRenderingContext.COMPILE_STATUS).asInstanceOf[Boolean]) {
      logger.error("An error occurred compiling the shaders: " + gl.getShaderInfoLog(shader))
      gl.deleteShader(shader)
      //TODO Throw Error
    }

    shader
  }

  private def resizeCanvasToDisplaySize(multiplier: Double = 1): Boolean = {
    val width = gl.canvas.clientWidth * multiplier
    val height = gl.canvas.clientHeight * multiplier
    if (gl.canvas.width != width || gl.canvas.height != height) {
      gl.canvas.width = width.toInt
      gl.canvas.height = height.toInt
      true
    } else {
      false
    }
  }
}
