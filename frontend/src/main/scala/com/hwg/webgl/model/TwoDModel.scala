package com.hwg.webgl.model

import com.hwg.util.MatrixStack
import com.hwg.webgl.{HwgWebGLProgram, TextureInfo}
import org.scalajs.dom.raw.{WebGLBuffer, WebGLRenderingContext}
import scryetek.vecmath.Mat4

import scala.scalajs.js

case class TwoDModel(texture: TextureInfo, gl: WebGLRenderingContext, width: Int, height: Int) extends Model {
  import com.hwg.util.TypedArrayConverters._
  import WebGLRenderingContext._

  val positionBuffer: WebGLBuffer = gl.createBuffer()
  gl.bindBuffer(ARRAY_BUFFER, positionBuffer)
  val positions = js.Array(
    1.0,  1.0, 1.0,
    -1.0,  1.0, 1.0,
    1.0, -1.0, 1.0,
    -1.0, -1.0, 1.0
  )
  gl.bufferData(ARRAY_BUFFER, Float32Array(positions), STATIC_DRAW)

  val normalBuffer: WebGLBuffer = gl.createBuffer()
  gl.bindBuffer(ARRAY_BUFFER, normalBuffer)
  val normals = js.Array(
  0, 0, 1,
  0, 0, 1,
  0, 0, 1,
  0, 0, 1
  )
  gl.bufferData(ARRAY_BUFFER, Float32Array(normals), STATIC_DRAW)

  val texcoordBuffer: WebGLBuffer = gl.createBuffer()
  gl.bindBuffer(ARRAY_BUFFER, texcoordBuffer)
  val texcoords = js.Array(
  0.0, 0.0,
  1.0, 0.0,
  0.0, 1.0,
  1.0, 1.0
  )
  gl.bufferData(ARRAY_BUFFER, Float32Array(texcoords), STATIC_DRAW)

  def draw(program: HwgWebGLProgram, matrixStack: MatrixStack, x: Double, y: Double): Unit = {

    gl.activeTexture(TEXTURE0)
    gl.bindTexture(TEXTURE_2D, this.texture.texture)
    gl.uniform1i(program.samplerUniform, 0)

    gl.bindBuffer(ARRAY_BUFFER, this.normalBuffer)
    gl.vertexAttribPointer(program.normalLocation, 3, FLOAT, normalized = false, 0, 0)

    gl.bindBuffer(ARRAY_BUFFER, this.positionBuffer)
    gl.vertexAttribPointer(program.positionLocation, 3, FLOAT, normalized = false, 0, 0)

    gl.bindBuffer(ARRAY_BUFFER, this.texcoordBuffer)
    gl.vertexAttribPointer(program.texcoordLocation, 2, FLOAT, normalized = false, 0, 0)

    //let matrix = mat4.ortho(mat4.create(), 0, gl.canvas.width, gl.canvas.height, 0, -1, 1);
    val matrix = Mat4()
    matrix.postMultiply(matrixStack.getCurrentMatrix)
    matrix.postScale(width.toFloat / 400, height.toFloat / 400, 1)

    program.setMatrixUniforms(matrix, x, y)

    gl.drawArrays(TRIANGLE_STRIP, 0, 4)
  }
}
