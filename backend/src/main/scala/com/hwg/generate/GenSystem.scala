package com.hwg.generate

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import com.hwg.background.{PlanetTexture, StarField, TextureOptions}
import com.hwg.util.Color
import com.hwg.util.noise.{CloudyNoise, SimplexNoise}

import scala.util.Random
import slogging.LazyLogging

object GenSystem extends LazyLogging {

  def generate(seed: Long, options: SystemOptions = SystemOptions()): Unit = {
    generateBackground(seed, options)

    val textureOptions = TextureOptions(
      1600,
      seed,
      grass = Color(0, 200, 20),
      desert = Color(222, 184, 135),
      sea = Color(0, 20, 200)
    )

    generatePlanets(seed, textureOptions)
  }

  def generateBackground(seed: Long, options: SystemOptions): Unit = {
    val random = new Random(seed)

    val s = System.currentTimeMillis()

    val starData = {
      val noiseFunc = {
        val noise = new CloudyNoise(new SimplexNoise(new Random(seed.toLong)), 8)
        (x: Double, y: Double) => noise.noise(x / options.backgroundSize, y / options.backgroundSize, 0) + 1
      }

      StarField.generateTexture(options.backgroundSize, options.backgroundSize, options.pointsNear, options.minRadius, options.maxRadius,
        noiseFunc, random, options.maxIterations)
    }

    val systemOut = new BufferedImage(options.backgroundSize, options.backgroundSize, BufferedImage.TYPE_INT_ARGB)

    for (x <- 0 until options.backgroundSize;
         y <- 0 until options.backgroundSize) {
      systemOut.setRGB(x, y, starData(x + y * options.backgroundSize).getRGB)
    }

    ImageIO.write(systemOut, "png", new File(s"./backend/src/main/resources/web/img/background_$seed.png"))

    logger.info(s"file written: '${s"./backend/src/main/resources/web/img/background_$seed.png"}'")
    logger.info(s"Background generation took ${(System.currentTimeMillis() - s) / 1000}s")
  }

  def generatePlanets(seed: Long, options: TextureOptions): Unit = {

    val s = System.currentTimeMillis()

    val planetData = PlanetTexture.generate(options)

    val systemOut = new BufferedImage(options.width, options.height, BufferedImage.TYPE_INT_RGB)

    for (x <- 0 until options.width;
         y <- 0 until options.height) {
      systemOut.setRGB(x, y, planetData(x + y * options.width).rgb)
    }

    ImageIO.write(systemOut, "png", new File(s"./backend/src/main/resources/web/img/planet_$seed.png"))

    logger.info(s"file written: '${s"./backend/src/main/resources/web/img/planet_$seed.png"}'")

    logger.info(s"Planet Texture Generation took ${(System.currentTimeMillis() - s) / 1000}s")
  }


}

case class SystemOptions(
                          backgroundSize: Int = 3072,
                          pointsNear: Int = 20,
                          minRadius: Int = 1,
                          maxRadius: Int = 25, // TODO Why does options effect how options looks so much?
                          maxIterations: Int = 50000
                        )