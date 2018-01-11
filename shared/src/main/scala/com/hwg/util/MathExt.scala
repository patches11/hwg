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
}
