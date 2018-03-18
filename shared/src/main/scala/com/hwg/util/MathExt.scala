package com.hwg.util

object MathExt {
  def genPointSquare(n: Int, includeOrigin: Boolean): Array[Point] = {
    val limit = Math.floor((n - 1) / 2).toInt

    val points = for (x <- -limit to limit;
         y <- -limit to limit) yield {
      Point(x, y)
    }

    if (includeOrigin) {
      points.toArray
    } else {
      points.filter(p => p.x != 0 || p.y != 0).toArray
    }
  }

  val TwoPi: Double = Math.PI * 2

  def arctan(y: Double, x: Double): Double = {
    if (y < 0) {
      (Math.atan(x / y) + Math.PI) % TwoPi
    } else {
      (Math.atan(x / y) + TwoPi) % TwoPi
    }
  }

  def distance(x1: Double, y1: Double, x2: Double, y2: Double): Double = {
    val xDiff = x1 - x2
    val yDiff = y1 - y2

    Math.sqrt(xDiff * xDiff + yDiff * yDiff)
  }

  def easeInOutCustom(t: Double, b: Double, c: Double, d: Double, p: Double): Double = {
    val tint = t / d * 2
    if (tint < 1)
      c / 2 * Math.pow(t, p) + b
    else {
      val t3 = tint - 2
      c / 2 * (Math.pow(t3, p) + 2) + b
    }
  }

  def easeInOutQuint(t: Double, b: Double, c: Double, d: Double): Double = {
    val tint = t / d * 2
    if (tint < 1)
      c / 2 * t * t * t * t * t + b
    else {
      val t3 = tint - 2
      c / 2 * (t3 * t3 * t3 * t3 * t3 + 2) + b
    }
  }

  def easeInOutQuart(t: Double, b: Double, c: Double, d: Double): Double = {
    val tint = t / d * 2
    if (tint < 1)
      c / 2 * t * t * t * t + b
    else {
      val t3 = tint - 2
      c / 2 * (t3 * t3 * t3 * t3 + 2) + b
    }
  }

  def easeInOutCubic(t: Double, b: Double, c: Double, d: Double): Double = {
    val tint = t / d * 2
    if (tint < 1)
      c / 2 * t * t * t + b
    else {
      val t3 = tint - 2
      c / 2 * (t3 * t3 * t3 + 2) + b
    }
  }

  def easeInOutQuad(t: Double, b: Double, c: Double, d: Double): Double = {
    val tint = t / d * 2
    if (tint < 1)
      c / 2 * t * t + b
    else {
      val t3 = tint - 2
      c / 2 * (t3 * t3 + 2) + b
    }
  }
}
