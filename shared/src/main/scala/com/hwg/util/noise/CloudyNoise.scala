
package com.hwg.util.noise

class CloudyNoise(baseNoise: Noise, octaves: Int) extends Noise {
  override def noise(x: Double, y: Double, z: Double): Double = {
    val (noise, amp) = (1 until octaves).foldLeft((0.0, 0.0)) { case ((noise, amp), i) =>
      val pow = Math.pow(2, i)
      (noise + baseNoise.noise(x * pow, y * pow, z) / pow, amp + 1 / pow)
    }

    noise / amp
  }
}
