package com.hwg.generate

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import com.hwg.background.{PlanetTexture, StarField, TextureOptions}
import com.hwg.universe.SystemConfig
import com.hwg.util.Color
import com.hwg.util.noise.{CloudyNoise, SimplexNoise}
import slogging.LazyLogging

import scala.util.Random

import java.io.File


object GenSystem extends LazyLogging {

  val baseDir = "./backend/src/main/resources/web/"

  def makePath(rest: String) = {
    baseDir + rest
  }

  def makeFile(rest: String) = {
    new File(baseDir + rest)
  }

  def setup(options: SystemOptions): Unit = {
    val dir = new File(makePath(options.config.dir))

    dir.mkdirs

    Option(dir.listFiles).map(_.toList).getOrElse(Nil).foreach(_.delete())
  }

  def generate(options: SystemOptions): Unit = {
    setup(options)

    generateBackground(options)

    options.config.planets.zipWithIndex.foreach { case (planetConfig, i) =>
      val textureOptions = TextureOptions(
        planetConfig,
        1600,
        options.config.seed * 7 * (i + 1),
        grass = Color(0, 120, 40),
        desert = Color(222, 184, 135),
        sea = Color(0, 20, 200)
      )

      generatePlanet(i, options.config, textureOptions)
    }
  }

  def generateBackground(options: SystemOptions): Unit = {
    logger.info("beginning background generation")
    val random = new Random(options.config.seed)

    val s = System.currentTimeMillis()

    val starData = {
      val noiseFunc = {
        val noise = new CloudyNoise(new SimplexNoise(new Random(options.config.seed)), 8)
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

    ImageIO.write(systemOut, "png", makeFile(options.config.background))

    logger.info(s"file written: '${makePath(options.config.background)}'")
    logger.info(s"Background generation took ${(System.currentTimeMillis() - s) / 1000}s")
  }

  def generatePlanet(index: Int, systemConfig: SystemConfig, options: TextureOptions): Unit = {
    logger.info(s"beginning planet generation: $index of ${systemConfig.planets.length}")

    val s = System.currentTimeMillis()

    val planetData = PlanetTexture.generate(options)

    val systemOut = new BufferedImage(options.width, options.height, BufferedImage.TYPE_INT_RGB)

    for (x <- 0 until options.width;
         y <- 0 until options.height) {
      systemOut.setRGB(x, y, planetData(x + y * options.width).rgb)
    }

    ImageIO.write(systemOut, "png", makeFile(systemConfig.tex(index)))

    logger.info(s"file written: '${makePath(systemConfig.tex(index))}'")

    logger.info(s"Planet Texture Generation took ${(System.currentTimeMillis() - s) / 1000}s")
  }


}

case class SystemOptions(
                          config: SystemConfig,
                          backgroundSize: Int = 3072,
                          pointsNear: Int = 20,
                          minRadius: Int = 1,
                          maxRadius: Int = 25, // TODO Why does options effect how options looks so much?
                          maxIterations: Int = 50000
                        )