package com.hwg.webgl

import org.scalajs.dom
import org.scalajs.dom.raw.{WebGLProgram, WebGLRenderingContext, WebGLShader}
import scryetek.vecmath.Mat4

case class HwgWebGLProgram(gl: WebGLRenderingContext, draw: () => Unit) {
  import com.hwg.util.VecmathConverters._
  private val GL = WebGLRenderingContext

  val program: WebGLProgram = initShaderProgram(Shaders.fShader, Shaders.vShader)

  gl.useProgram(program)

  val positionLocation = gl.getAttribLocation(program, "aVertexPosition")
  val texcoordLocation = gl.getAttribLocation(program, "aTextureCoord")
  val normalLocation = gl.getAttribLocation(program, "aVertexNormal")

  val perspectiveMatrix = gl.getUniformLocation(program, "uProjectionMatrix")
  val moveMatrix = gl.getUniformLocation(program, "uModelViewMatrix")
  val normalMatrix = gl.getUniformLocation(program, "uNormalMatrix")

  val ambientColor = gl.getUniformLocation(program, "uAmbientColor")
  val lightingDir = gl.getUniformLocation(program, "uLightingDirection")
  val dirColor = gl.getUniformLocation(program, "uDirectionalColor")

  val samplerUniform = gl.getUniformLocation(program, "uSampler")

  // Fill BG
  gl.clearColor(0.0, 0.0, 0.0, 1.0)

  // Enable Alpha
  gl.blendFunc(GL.SRC_ALPHA, GL.ONE_MINUS_SRC_ALPHA)
  gl.enable(GL.BLEND)

  gl.enable(GL.DEPTH_TEST)           // Enable depth testing
  gl.depthFunc(GL.LEQUAL)            // Near things obscure far things

  gl.clear(GL.COLOR_BUFFER_BIT | GL.DEPTH_BUFFER_BIT)

  dom.window.requestAnimationFrame(new scala.scalajs.js.Function1[Double, _] {
    def apply(a: Double): Unit = {
      resizeCanvasToDisplaySize()
      draw()
      dom.window.requestAnimationFrame(this)
    }
  })

  def setMatrixUniforms(mvMatrix: Mat4, x: Double, y: Double): Unit = {
    gl.uniformMatrix4fv(moveMatrix, transpose = false, mvMatrix.toJsArray)

    this.setCamera(x, y)

    val normalMatrix = mvMatrix.copy()
    normalMatrix.invert()
    normalMatrix.transpose()

    gl.uniformMatrix4fv(this.normalMatrix, transpose = false, normalMatrix.toJsArray)
  }

  def setCamera(x: Double, y: Double, z: Double = 0): Unit = {

    val cameraMatrix = Mat4().postTranslate(x.toFloat, y.toFloat, z.toFloat)
    val viewMatrix = cameraMatrix.invert(Mat4())

    val pMatrix = Mat4.perspective((Math.PI / 4).toFloat, gl.canvas.width / gl.canvas.height, 0.1f, 100.0f)

    val pvMatrix = pMatrix.postMultiply(viewMatrix)

    gl.uniformMatrix4fv(this.perspectiveMatrix, transpose = false, pvMatrix.toJsArray)
  }

  private def initShaderProgram(vsSource: String, fsSource: String): WebGLProgram = {
    val vertexShader = loadShader(WebGLRenderingContext.VERTEX_SHADER, vsSource)
    val fragmentShader = loadShader(WebGLRenderingContext.FRAGMENT_SHADER, fsSource)

    // Create the shader program

    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertexShader)
    gl.attachShader(shaderProgram, fragmentShader)
    gl.linkProgram(shaderProgram)

    // If creating the shader program failed, alert

    if (!gl.getProgramParameter(shaderProgram, WebGLRenderingContext.LINK_STATUS).asInstanceOf[Boolean]) {
      println("Unable to initialize the shader program: " + gl.getProgramInfoLog(shaderProgram))
      // TODO Throw Error
    }

    shaderProgram
  }

  private def loadShader(shaderType: Int, source: String): WebGLShader = {
    val shader = gl.createShader(WebGLRenderingContext.VERTEX_SHADER)

    // Send the source to the shader object

    gl.shaderSource(shader, source)

    // Compile the shader program

    gl.compileShader(shader)

    // See if it compiled successfully

    if (!gl.getShaderParameter(shader, WebGLRenderingContext.COMPILE_STATUS).asInstanceOf[Boolean]) {
      println("An error occurred compiling the shaders: " + gl.getShaderInfoLog(shader))
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
