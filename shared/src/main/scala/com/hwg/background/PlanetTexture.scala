package com.hwg.background

import com.hwg.universe.PlanetConfig
import com.hwg.util.noise.{CloudyNoise, SimplexNoise}
import com.hwg.util.{Color, MathExt}

import scala.util.Random

object PlanetTexture {
  import BackgroundUtils._
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

        val darkenSizeRes = (darkenSizeNoise(spherePoint.x / options.darkenSizeSpeedFactor, spherePoint.y / options.darkenSizeSpeedFactor, spherePoint.z / options.darkenSizeSpeedFactor) + 1) / 2
        val darkenSize = darkenSizeRes * options.darkenSizeFactor + options.darkenSizeMin
        val darken = Math.pow((darkenNoise(spherePoint.x * darkenSize, spherePoint.y * darkenSize, spherePoint.z * darkenSize) + 1) / 2, 2) - 0.5

        options.grass.blend(options.desert, easedBlend).reverseAdjust(darken)
      }

      val eased = MathExt.easeInOutCubic(cloudValue, 0, 1, 1)

      data(index) = color.optionally(eased > options.cloudMin)(_.blend(Color.white, eased))
    }

    data
  }



}

case class TextureOptions(
                           config: PlanetConfig,
                           size: Int,
                           seed: Long,
                           grass: Color,
                           desert: Color,
                           sea: Color,
                           octaves: Int = 16,
                           waterPercent: Double = 0.5,
                           cloudSeed: Option[Long] = None,
                           heightSeed: Option[Long] = None,
                           cloudSizeSeed: Option[Long] = None,
                           darkenSeed: Option[Long] = None,
                           cloudSizeFactor: Double = 6,
                           cloudSmallSkew: Double = 3,
                           cloudSizeSpeedFactor: Double = 1.0,
                           darkenSizeSpeedFactor: Double = 3,
                           darkenSizeFactor: Double = 4,
                           darkenSizeMin: Double = 4,
                           cloudMin: Double = 0.1
                         ) {
  def width: Int = size

  def height: Int = size / 2

  def getHeightSeed: Long = heightSeed.getOrElse(seed * 11)

  def getCloudSizeSeed: Long = cloudSizeSeed.getOrElse(getCloudSeed * 7)

  def getCloudSeed: Long = cloudSeed.getOrElse(seed * 3)

  def getDarkenSeed: Long = darkenSeed.getOrElse(seed * 31)
}

case class Point3d(x: Double, y: Double, z: Double)