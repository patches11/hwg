package com.hwg.webgl.model

import com.hwg.util.MatrixStack
import com.hwg.webgl.{HwgWebGLProgram, TextureInfo}
import org.scalajs.dom.raw.{WebGLBuffer, WebGLRenderingContext}
import scryetek.vecmath.Mat4

case class TwoDModel(texture: TextureInfo, gl: WebGLRenderingContext, width: Int, height: Int) extends Model {
  import com.hwg.util.TypedArrayConverters._

  private val GL = WebGLRenderingContext

  val positionBuffer: WebGLBuffer = gl.createBuffer()
  gl.bindBuffer(GL.ARRAY_BUFFER, positionBuffer)
  val positions = Array(
    1.0,  1.0, 1.0,
    -1.0,  1.0, 1.0,
    1.0, -1.0, 1.0,
    -1.0, -1.0, 1.0
  )
  gl.bufferData(GL.ARRAY_BUFFER, Float32Array(positions), GL.STATIC_DRAW)

  val normalBuffer: WebGLBuffer = gl.createBuffer()
  gl.bindBuffer(GL.ARRAY_BUFFER, normalBuffer)
  val normals = Array(
  0, 0, 1,
  0, 0, 1,
  0, 0, 1,
  0, 0, 1
  )
  gl.bufferData(GL.ARRAY_BUFFER, Float32Array(normals), GL.STATIC_DRAW);

  val texcoordBuffer: WebGLBuffer = gl.createBuffer()
  gl.bindBuffer(GL.ARRAY_BUFFER, texcoordBuffer)
  val texcoords = Array(
  0.0, 0.0,
  1.0, 0.0,
  0.0, 1.0,
  1.0, 1.0
  )
  gl.bufferData(GL.ARRAY_BUFFER, Float32Array(texcoords), GL.STATIC_DRAW)

  def draw(program: HwgWebGLProgram, matrixStack: MatrixStack, x: Double, y: Double): Unit = {

    gl.activeTexture(GL.TEXTURE0)
    gl.bindTexture(GL.TEXTURE_2D, this.texture.texture)
    gl.uniform1i(program.samplerUniform, 0)

    gl.bindBuffer(GL.ARRAY_BUFFER, this.normalBuffer)
    gl.vertexAttribPointer(program.normalLocation, 3, GL.FLOAT, normalized = false, 0, 0)

    gl.bindBuffer(GL.ARRAY_BUFFER, this.positionBuffer)
    gl.vertexAttribPointer(program.positionLocation, 3, GL.FLOAT, normalized = false, 0, 0)

    gl.bindBuffer(GL.ARRAY_BUFFER, this.texcoordBuffer)
    gl.vertexAttribPointer(program.texcoordLocation, 2, GL.FLOAT, normalized = false, 0, 0)

    //let matrix = mat4.ortho(mat4.create(), 0, gl.canvas.width, gl.canvas.height, 0, -1, 1);
    val matrix = Mat4()
    matrix.postMultiply(matrixStack.getCurrentMatrix)
    matrix.postScale(width / 400, height / 400, 1)

    program.setMatrixUniforms(matrix, x, y)

    gl.drawArrays(GL.TRIANGLE_STRIP, 0, 4)
  }
}
