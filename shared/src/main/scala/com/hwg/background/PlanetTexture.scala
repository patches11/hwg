package com.hwg.background

import com.hwg.util.{Color, MathExt}
import com.hwg.util.noise.{CloudyNoise, SimplexNoise}

import scala.util.Random

object PlanetTexture {
  import com.hwg.util.Optionally._

  def generate(options: TextureOptions): Array[Color] = {
    val noise = getNoiseFunction(options.seed, options.octaves)
    val cloudNoise = getNoiseFunction(options.getCloudSeed, options.octaves)
    val cloudNoise2 = getNoiseFunction(options.getCloudSizeSeed, options.octaves)

    val heightNoise = getNoiseFunction(options.getHeightSeed, options.octaves)

    val darkenSizeNoise = getNoiseFunction(options.getDarkenSeed * 7, options.octaves)
    val darkenNoise = getNoiseFunction(options.getDarkenSeed, options.octaves)

    val data = new Array[Color](options.width * options.height)

    for {
      x <- 0 until options.width
      y <- 0 until options.height
    } yield {
      val index = x + y * options.width

      val spherePoint = sphereMap(x.toDouble / options.width, y.toDouble / options.height)

      val noiseValue = (noise(spherePoint.x, spherePoint.y, spherePoint.z) + 1) / 2

      val cloudSizeRes = (cloudNoise2(spherePoint.x / options.cloudSizeSpeedFactor, spherePoint.y / options.cloudSizeSpeedFactor, spherePoint.z / options.cloudSizeSpeedFactor) + 1) / 2
      val cloudSize = Math.pow(cloudSizeRes, options.cloudSmallSkew) * options.cloudSizeFactor // smaller size is larger
      val cloudValue = (cloudNoise(spherePoint.x * cloudSize, spherePoint.y * cloudSize, spherePoint.z * cloudSize) + 1) / 2

      val color = if (noiseValue < options.waterPercent) {
        val adjusted = Math.pow(noiseValue / options.waterPercent, 5)
        options.sea.adjust(adjusted - 0.5)
      } else {
        val heightValue = (heightNoise(spherePoint.x, spherePoint.y, spherePoint.z) + 1) / 2

        val easedBlend = MathExt.easeInOutCubic(heightValue, 0, 1, 1)

        val darkenSizeSpeedFactor = 3
        val darkenSizeFactor = 4
        val darkenSizeRes = (darkenSizeNoise(spherePoint.x / darkenSizeSpeedFactor, spherePoint.y / darkenSizeSpeedFactor, spherePoint.z / darkenSizeSpeedFactor) + 1) / 2
        val darkenSize = darkenSizeRes * darkenSizeFactor + 4
        val darken = Math.pow((darkenNoise(spherePoint.x * darkenSize, spherePoint.y * darkenSize, spherePoint.z * darkenSize) + 1) / 2, 2) - 0.5

        options.grass.blend(options.desert, easedBlend).reverseAdjust(darken)
      }

      val eased = MathExt.easeInOutCubic(cloudValue, 0, 1, 1)

      data(index) = color.optionally(eased > 0.1)(_.blend(Color.white, eased))
    }

    data
  }

  private def sphereMap(u: Double, v: Double): Point3d = {
    val azimuth = 2 * Math.PI * u
    val inclination = Math.PI * v
    val init = if (v <= 0.5) v * 2 else 2 - v * 2
    val x = init * Math.cos(azimuth)
    val y = init * Math.sin(azimuth)
    val z = Math.cos(inclination)
    Point3d(x, y, z)
  }

  private def getNoiseFunction(seed: Double, octaves: Int): (Double, Double, Double) => Double = {
    val noise = new CloudyNoise(new SimplexNoise(new Random(seed.toLong)), octaves)
    (x: Double, y: Double, z: Double) => noise.noise(x, y, z)
  }


}

case class TextureOptions(
                           size: Int,
                           seed: Double,
                           grass: Color,
                           desert: Color,
                           sea: Color,
                           octaves: Int = 16,
                           waterPercent: Double = 0.5,
                           cloudSeed: Option[Double] = None,
                           heightSeed: Option[Double] = None,
                           cloudSizeSeed: Option[Double] = None,
                           darkenSeed: Option[Double] = None,
                           cloudSizeFactor: Double = 6,
                           cloudSmallSkew: Double = 3,
                           cloudSizeSpeedFactor: Double = 1.0
                         ) {
  def width: Int = size

  def height: Int = size / 2

  def getCloudSeed: Double = cloudSeed.getOrElse(seed * 3)

  def getHeightSeed: Double = heightSeed.getOrElse(seed * 11)

  def getCloudSizeSeed: Double = cloudSizeSeed.getOrElse(getCloudSeed * 7)

  def getDarkenSeed: Double = darkenSeed.getOrElse(seed * 31)
}

case class Point3d(x: Double, y: Double, z: Double)