package com.hwg.webgl.model

import com.hwg.util.MatrixStack
import com.hwg.webgl.{HwgWebGLProgram, TextureInfo}
import org.scalajs.dom.raw.WebGLBuffer

case class BufferWrapper(buffer: WebGLBuffer, itemSize: Int, numItems: Int)

trait Model {
  val texture: TextureInfo

  def draw(program: HwgWebGLProgram, matrixStack: MatrixStack, x: Double, y: Double): Unit
}