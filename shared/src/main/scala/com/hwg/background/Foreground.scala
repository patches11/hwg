package com.hwg.background

import com.hwg.util.{Color}

object Foreground {
  import BackgroundUtils._

  def generate(options: ForegroundOptions): Array[Color] = {
    val noise = getNoiseFunctionC(options.seed, options.octaves).tupled

    val data = new Array[Color](options.size * options.size)

    for {
      x <- 0 until options.size
      y <- 0 until options.size
    } {
      val index = x + y * options.size

      val n = (noise(loc(x, y, options)) + 1) / 2

      val adj = Math.pow(n, 2)

      data(index) = Color(255, 255, 255, (adj * options.weight).toShort)
    }

    data
  }

  private def loc(x: Int, y: Int, options: ForegroundOptions): (Double, Double, Double) = {
    val adjX = x.toDouble / options.size
    val adjY = y.toDouble / options.size

    val xx = f(adjX, options)
    val yy = f(adjY, options)
    val z = fz(adjX, adjY)
    (xx, yy, z)
  }

  private def f(adj: Double, options: ForegroundOptions): Double = {
    if (adj < 0.5)
      adj * options.scale
    else
      (1 - adj) * options.scale
  }

  private def fz(x: Double, y: Double): Double = {
    Math.sin(x * Math.PI * 2) + Math.sin(y * Math.PI * 2)
  }
}

case class ForegroundOptions(
                              seed: Long,
                              size: Int,
                              octaves: Int = 12,
                              z: Double = 0,
                              scale: Int = 8,
                              weight: Int = 128
                            )