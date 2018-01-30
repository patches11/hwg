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
}
