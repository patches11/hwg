package com.hwg.util

import scryetek.vecmath.{Mat4, Vec3}

import scala.collection.mutable.ListBuffer

class MatrixStack {
  val stack: ListBuffer[Mat4] = ListBuffer()

  private def pop: Mat4 = stack.remove(0)

  private def push(m: Mat4) = stack.prepend(m)

  def restore(): Unit = {
    pop
    // Never let the stack be totally empty
    if (this.stack.length < 1) {
      push(new Mat4)
    }
  }

  // Pushes a copy of the current matrix on the stack
  def save(): Unit = {
    push(this.getCurrentMatrix)
  }

  // Gets a copy of the current matrix (top of the stack)
  def getCurrentMatrix: Mat4 = {
    stack.head
  }

  // Lets us set the current matrix
  def setCurrentMatrix(m: Mat4): Mat4 = {
    stack(0) = m
    m
  }

  // Translates the current matrix
  def translate(x: Double, y: Double, z: Double = 0): Unit = {
    val m = getCurrentMatrix
    this.setCurrentMatrix(m.postTranslate(x.toFloat, y.toFloat, z.toFloat))
  }

  // Rotates the current matrix around Z
  def rotateZ(angleInRadians: Double): Unit = {
    val m = getCurrentMatrix
    this.setCurrentMatrix(m.postRotateZ(angleInRadians.toFloat))
  }

  def rotateX(angleInRadians: Double): Unit = {
    val m = getCurrentMatrix
    this.setCurrentMatrix(m.postRotateX(angleInRadians.toFloat))
  }

  def rotate(axis: (Double, Double, Double), radians: Double): Unit = {
    val m = getCurrentMatrix
    val a = Vec3(axis._1.toFloat, axis._2.toFloat, axis._3.toFloat)
    this.setCurrentMatrix(m.postRotate(radians.toFloat, a))
  }

  // Scales the current matrix
  def scale(x: Double, y: Double, z: Double = 1): Unit = {
    val m = getCurrentMatrix
    this.setCurrentMatrix(m.postScale(x.toFloat, y.toFloat, z.toFloat))
  }
}
