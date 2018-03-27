package com.hwg.background

import com.hwg.util.noise.{ClassicNoise, CloudyNoise, SimplexNoise}

import scala.util.Random

object BackgroundUtils {

  def sphereMap(u: Double, v: Double): Point3d = {
    val azimuth = 2 * Math.PI * u
    val inclination = Math.PI * v
    val init = if (v <= 0.5) v * 2 else 2 - v * 2
    val x = init * Math.cos(azimuth)
    val y = init * Math.sin(azimuth)
    val z = Math.cos(inclination)
    Point3d(x, y, z)
  }

  def getNoiseFunction(seed: Long, octaves: Int): (Double, Double, Double) => Double = {
    val noise = new CloudyNoise(new SimplexNoise(new Random(seed)), octaves)
    (x: Double, y: Double, z: Double) => noise.noise(x, y, z)
  }

  def getNoiseFunctionC(seed: Long, octaves: Int): (Double, Double, Double) => Double = {
    val noise = new CloudyNoise(new ClassicNoise(new Random(seed)), octaves)
    (x: Double, y: Double, z: Double) => noise.noise(x, y, z)
  }
}
