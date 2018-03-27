package com.hwg.webgl

import com.hwg.util.MatrixStack

case class Draw(z: Double, drawFunc: (MatrixStack) => Unit) extends Ordered[Draw] {
  def compare( other: Draw ) = if (z < other.z) -1 else 1

  def draw(matrixStack: MatrixStack): Unit = {
    matrixStack.save()
    drawFunc(matrixStack)
    matrixStack.restore()
  }
}