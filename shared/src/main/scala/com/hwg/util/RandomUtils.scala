package com.hwg.util

import scala.util.Random

object RandomUtils {

  implicit class RandomFuncs(random: Random) {
    def in(range: Range): Int = {
      val size = range.max - range.min
      range.min + random.nextInt(size)
    }

    def in(start: Double, end: Double): Double = {
      val size = end - start
      start + random.nextDouble() * size
    }
  }
}