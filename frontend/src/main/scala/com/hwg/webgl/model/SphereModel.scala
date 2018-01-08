package com.hwg.webgl.model

import com.hwg.util.MatrixStack
import com.hwg.webgl.{HwgWebGLProgram, TextureInfo}
import org.scalajs.dom.raw.WebGLRenderingContext

import scala.collection.mutable.ListBuffer

case class SphereModel(texture: TextureInfo, gl: WebGLRenderingContext) extends Model {
  import com.hwg.util.TypedArrayConverters._

  private val GL = WebGLRenderingContext

  val latitudeBands = 30
  val longitudeBands = 30
  val radius = 2

  val vertexPositionData: ListBuffer[Double] = ListBuffer()
  val normalData: ListBuffer[Double] = ListBuffer()
  val textureCoordData: ListBuffer[Double] = ListBuffer()
  val indexData: ListBuffer[Int] = ListBuffer()

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
      val u = 1 - (longNumber / this.longitudeBands)
      val v = 1 - (latNumber / this.latitudeBands)
      normalData.append(x)
      normalData.append(y)
      normalData.append(z)
      textureCoordData.append(u)
      textureCoordData.append(v)
      vertexPositionData.append(this.radius * x)
      vertexPositionData.append(this.radius * y)
      vertexPositionData.append(this.radius * z)
    }
  }

  (0 to latitudeBands).foreach { latNumber =>
    (0 to longitudeBands).foreach { longNumber =>
      val first = (latNumber * (this.longitudeBands + 1)) + longNumber
      val second = first + this.longitudeBands + 1
      indexData.append(first)
      indexData.append(second)
      indexData.append(first + 1)
      indexData.append(second)
      indexData.append(second + 1)
      indexData.append(first + 1)
    }
  }

  private val moonVertexNormalB = gl.createBuffer()
  gl.bindBuffer(GL.ARRAY_BUFFER, moonVertexNormalB)
  gl.bufferData(GL.ARRAY_BUFFER, Float32Array(normalData.toList), GL.STATIC_DRAW)

  private val moonVertexTextureCoordB = gl.createBuffer()
  gl.bindBuffer(GL.ARRAY_BUFFER, moonVertexTextureCoordB)
  gl.bufferData(GL.ARRAY_BUFFER, Float32Array(textureCoordData.toList), GL.STATIC_DRAW)

  private val moonVertexPositionB = gl.createBuffer()
  gl.bindBuffer(GL.ARRAY_BUFFER, moonVertexPositionB)
  gl.bufferData(GL.ARRAY_BUFFER, Float32Array(vertexPositionData.toList), GL.STATIC_DRAW)

  private val moonVertexIndexB = gl.createBuffer()
  gl.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, moonVertexIndexB)
  gl.bufferData(GL.ELEMENT_ARRAY_BUFFER, Uint16Array(indexData.toList), GL.STATIC_DRAW)


  val moonVertexNormalBuffer: BufferWrapper = BufferWrapper(moonVertexNormalB, 3, normalData.length / 3)
  val moonVertexTextureCoordBuffer: BufferWrapper = BufferWrapper(moonVertexTextureCoordB, 2, normalData.length / 2)
  val moonVertexPositionBuffer: BufferWrapper = BufferWrapper(moonVertexPositionB, 3, normalData.length / 3)
  val moonVertexIndexBuffer: BufferWrapper = BufferWrapper(moonVertexIndexB, 1, normalData.length)

  def draw(program: HwgWebGLProgram, matrixStack: MatrixStack, x: Double, y: Double): Unit = {
    gl.activeTexture(GL.TEXTURE0)
    gl.bindTexture(GL.TEXTURE_2D, this.texture.texture)
    gl.uniform1i(program.samplerUniform, 0)

    gl.bindBuffer(GL.ARRAY_BUFFER, this.moonVertexPositionBuffer.buffer)
    gl.vertexAttribPointer(program.positionLocation, this.moonVertexPositionBuffer.itemSize, GL.FLOAT, normalized = false, 0, 0)

    gl.bindBuffer(GL.ARRAY_BUFFER, this.moonVertexTextureCoordBuffer.buffer)
    gl.vertexAttribPointer(program.texcoordLocation, this.moonVertexTextureCoordBuffer.itemSize, GL.FLOAT, normalized = false, 0, 0)

    gl.bindBuffer(GL.ARRAY_BUFFER, this.moonVertexNormalBuffer.buffer)
    gl.vertexAttribPointer(program.normalLocation, this.moonVertexNormalBuffer.itemSize, GL.FLOAT, normalized = false, 0, 0)

    gl.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, this.moonVertexIndexBuffer.buffer)
    program.setMatrixUniforms(matrixStack.getCurrentMatrix, x, y)
    gl.drawElements(GL.TRIANGLES, this.moonVertexIndexBuffer.numItems, GL.UNSIGNED_SHORT, 0)
  }
}
