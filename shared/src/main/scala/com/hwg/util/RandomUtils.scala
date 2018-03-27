package com.hwg.util

import scala.util.Random

object RandomUtils {

  implicit class RandomFuncs(random: Random) {
    def in(range: Range): Int = {
      val size = range.max - range.min
      if (size == 0) {
        range.min
      } else {
        range.min + random.nextInt(size)
      }
    }

    def in(start: Double, end: Double): Double = {
      val size = end - start
      start + random.nextDouble() * size
    }

    def color(r: Range, g: Range, b: Range): Color = {
      Color(in(r).toShort, in(g).toShort, in(b).toShort)
    }
  }
}