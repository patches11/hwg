package com.hwg.util


import scala.collection.mutable.ListBuffer

class MatrixStack {
  val stack: ListBuffer[Mat4]

  private def pop: Matrix = stack.remove(0)

  private def push(m: Matrix) = stack.prepend(m)

  def restore(): Unit = {
    pop
    // Never let the stack be totally empty
    if (this.stack.length < 1) {
      push(new Matrix)
    }
  }

  // Pushes a copy of the current matrix on the stack
  def save(): Unit = {
    push(this.getCurrentMatrix)
  }

  // Gets a copy of the current matrix (top of the stack)
  def getCurrentMatrix: Matrix = {
    stack.head
  }

  // Lets us set the current matrix
  def setCurrentMatrix(m: Matrix): Matrix = {
    stack(0) = m
    m
  }

  // Translates the current matrix
  def translate(x: Double, y: Double, z: Double = 0): Unit = {
    val m = getCurrentMatrix
    this.setCurrentMatrix(mat4.translate(mat4.create(), m, [x, y, z]))
  }

  // Rotates the current matrix around Z
  def rotateZ(angleInRadians: number): Unit = {
    val m = getCurrentMatrix
    this.setCurrentMatrix(mat4.rotateZ(mat4.create(), m, angleInRadians))
  }

  def rotateX(angleInRadians: number): Unit = {
    val m = getCurrentMatrix
    this.setCurrentMatrix(mat4.rotateX(mat4.create(), m, angleInRadians))
  }

  def rotate(axis: number[], radians: number): Unit = {
    val m = getCurrentMatrix
    this.setCurrentMatrix(mat4.rotate(mat4.create(), m, radians, axis))
  }

  // Scales the current matrix
  def scale(x: number, y: number, z: number = 1): Unit = {
    val m = getCurrentMatrix
    this.setCurrentMatrix(mat4.scale(mat4.create(), m, [x, y, z]))
  }
}
