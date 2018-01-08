package com.hwg.webgl.model

import com.hwg.webgl.{HwgWebGLProgram, MatrixStack, TextureInfo}
import org.scalajs.dom.raw.WebGLBuffer

case class BufferWrapper(buffer: WebGLBuffer, itemSize: Int, numItems: Int)

trait Model {
  val texture: TextureInfo
  val program: HwgWebGLProgram

  def draw(matrixStack: MatrixStack, x: Int, y: Int): Unit
}