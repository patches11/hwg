package com.hwg.webgl.model

import com.hwg.util.MatrixStack
import com.hwg.webgl.{HwgWebGLProgram, TextureInfo}
import org.scalajs.dom.raw.WebGLRenderingContext

import scala.scalajs.js

case class SphereModel(texture: TextureInfo, gl: WebGLRenderingContext, radius: Double = 2) extends Model {
  import com.hwg.util.TypedArrayConverters._
  import WebGLRenderingContext._

  val latitudeBands = 30
  val longitudeBands = 30

  val vertexPositionData: js.Array[Double] = js.Array()
  val normalData: js.Array[Double] = js.Array()
  val textureCoordData: js.Array[Double] = js.Array()
  val indexData: js.Array[Int] = js.Array()

  (0 to latitudeBands).foreach { latNumber =>
    val theta = latNumber * Math.PI / this.latitudeBands
    val sinTheta = Math.sin(theta)
    val cosTheta = Math.cos(theta)
    (0 to longitudeBands).foreach { longNumber =>
      val phi = longNumber * 2 * Math.PI / this.longitudeBands
      val sinPhi = Math.sin(phi)
      val cosPhi = Math.cos(phi)
      val x = cosPhi * sinTheta
      val y = cosTheta
      val z = sinPhi * sinTheta
      val u = 1 - (longNumber.toDouble / this.longitudeBands)
      val v = 1 - (latNumber.toDouble / this.latitudeBands)
      normalData.push(x)
      normalData.push(y)
      normalData.push(z)
      textureCoordData.push(u)
      textureCoordData.push(v)
      vertexPositionData.push(radius * x)
      vertexPositionData.push(radius * y)
      vertexPositionData.push(radius * z)
    }
  }

  (0 until latitudeBands).foreach { latNumber =>
    (0 until longitudeBands).foreach { longNumber =>
      val first = (latNumber * (this.longitudeBands + 1)) + longNumber
      val second = first + this.longitudeBands + 1
      indexData.push(first)
      indexData.push(second)
      indexData.push(first + 1)
      indexData.push(second)
      indexData.push(second + 1)
      indexData.push(first + 1)
    }
  }

  private val moonVertexNormalB = gl.createBuffer()
  gl.bindBuffer(ARRAY_BUFFER, moonVertexNormalB)
  gl.bufferData(ARRAY_BUFFER, Float32Array(normalData), STATIC_DRAW)

  private val moonVertexTextureCoordB = gl.createBuffer()
  gl.bindBuffer(ARRAY_BUFFER, moonVertexTextureCoordB)
  gl.bufferData(ARRAY_BUFFER, Float32Array(textureCoordData), STATIC_DRAW)

  private val moonVertexPositionB = gl.createBuffer()
  gl.bindBuffer(ARRAY_BUFFER, moonVertexPositionB)
  gl.bufferData(ARRAY_BUFFER, Float32Array(vertexPositionData), STATIC_DRAW)

  private val moonVertexIndexB = gl.createBuffer()
  gl.bindBuffer(ELEMENT_ARRAY_BUFFER, moonVertexIndexB)
  gl.bufferData(ELEMENT_ARRAY_BUFFER, Uint16Array(indexData), STATIC_DRAW)


  val moonVertexNormalBuffer: BufferWrapper = BufferWrapper(moonVertexNormalB, 3, normalData.length / 3)
  val moonVertexTextureCoordBuffer: BufferWrapper = BufferWrapper(moonVertexTextureCoordB, 2, textureCoordData.length / 2)
  val moonVertexPositionBuffer: BufferWrapper = BufferWrapper(moonVertexPositionB, 3, vertexPositionData.length / 3)
  val moonVertexIndexBuffer: BufferWrapper = BufferWrapper(moonVertexIndexB, 1, indexData.length)

  def draw(program: HwgWebGLProgram, matrixStack: MatrixStack, x: Double, y: Double): Unit = {
    gl.activeTexture(TEXTURE0)
    gl.bindTexture(TEXTURE_2D, this.texture.texture)
    gl.uniform1i(program.samplerUniform, 0)

    gl.bindBuffer(ARRAY_BUFFER, this.moonVertexPositionBuffer.buffer)
    gl.vertexAttribPointer(program.positionLocation, this.moonVertexPositionBuffer.itemSize, FLOAT, normalized = false, 0, 0)

    gl.bindBuffer(ARRAY_BUFFER, this.moonVertexTextureCoordBuffer.buffer)
    gl.vertexAttribPointer(program.texcoordLocation, this.moonVertexTextureCoordBuffer.itemSize, FLOAT, normalized = false, 0, 0)

    gl.bindBuffer(ARRAY_BUFFER, this.moonVertexNormalBuffer.buffer)
    gl.vertexAttribPointer(program.normalLocation, this.moonVertexNormalBuffer.itemSize, FLOAT, normalized = false, 0, 0)

    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, this.moonVertexIndexBuffer.buffer)
    program.setMatrixUniforms(matrixStack.getCurrentMatrix, x, y)
    gl.drawElements(TRIANGLES, this.moonVertexIndexBuffer.numItems, UNSIGNED_SHORT, 0)
  }
}
