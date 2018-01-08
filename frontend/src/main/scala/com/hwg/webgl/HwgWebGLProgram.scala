package com.hwg.webgl

import org.scalajs.dom
import org.scalajs.dom.raw.{WebGLProgram, WebGLRenderingContext, WebGLShader}
import org.scalajs.dom.svg.Matrix

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

case class HwgWebGLProgram(gl: WebGLRenderingContext, draw: () => Unit) {
  val GL = WebGLRenderingContext

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

  dom.window.requestAnimationFrame(new Function1[Double, _] {
    def apply(a: Double): Unit = {
      resizeCanvasToDisplaySize()
      draw()
      dom.window.requestAnimationFrame(this)
    }
  })

  def setMatrixUniforms(mvMatrix: Float32Array, x: Double, y: Double): Unit = {
    gl.uniformMatrix4fv(moveMatrix, false, mvMatrix)

    this.setCamera(x, y)

    val normalMatrix = new Float32Array(1)
    //mat4.invert(normalMatrix, mvMatrix)
    //mat4.transpose(normalMatrix, normalMatrix)

    gl.uniformMatrix4fv(this.normalMatrix, false, normalMatrix)
  }

  def setCamera(x: Double, y: Double, z: Double = 0): Unit = {
    val pMatrix = mat4.create()


    val cameraMatrix = mat4.translate(mat4.create(), mat4.create(), [x, y, z])
    val viewMatrix = mat4.invert(mat4.create(), cameraMatrix)

    mat4.perspective(pMatrix, Math.PI / 4, gl.canvas.width / gl.canvas.height, 0.1, 100.0)

    val pvMatrix = viewMatrix == null ? pMatrix : mat4.multiply(mat4.create(), pMatrix, viewMatrix)

    gl.uniformMatrix4fv(this.perspectiveMatrix, false, pvMatrix)
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
    }
    false
  }
}
