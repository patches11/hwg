package com.hwg.webgl

import com.hwg.util.MatrixStack

import scala.collection.{GenTraversableOnce, mutable}

case class Draw(z: Double, drawFunc: (MatrixStack) => Unit) extends Ordered[Draw] {
  def compare( other: Draw ) = if (z < other.z) -1 else 1

  def draw(matrixStack: MatrixStack): Unit = {
    matrixStack.save()
    drawFunc(matrixStack)
    matrixStack.restore()
  }
}

class DrawContext() {
  private val set = mutable.SortedSet[Draw]()

  case class DrawSubContext(op: (MatrixStack => Unit)) {
    def at(z: Double): Unit = {
      set.add(Draw(
        z,
        op
      ))
    }
  }

  def +=(draw: Draw): Unit = set.add(draw)
  def ++=(draws: GenTraversableOnce[Draw]): Unit = draws.foreach(set.add)

  def apply(op: (MatrixStack => Unit)): DrawSubContext = {
    DrawSubContext(op)
  }

  def clear(): Unit = {
    set.clear()
  }

  def execute(matrixStack: MatrixStack): Unit = {
    set.foreach { d =>
      d.draw(matrixStack)
    }

    set.clear()
  }
}