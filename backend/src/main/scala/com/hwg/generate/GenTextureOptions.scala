package com.hwg.generate

import com.hwg.background.TextureOptions
import com.hwg.universe.PlanetConfig

import scala.util.Random

object GenTextureOptions {
  import com.hwg.util.RandomUtils._

  def generate(seed: Long, planetConfig: PlanetConfig): TextureOptions = {
    val random = new Random(seed)

    TextureOptions(
      planetConfig,
      1600,
      seed = random.nextLong(),
      grass = random.color(0 to 0, 50 to 120, 0 to 100),
      desert = random.color(100 to 222, 50 to 184, 20 to 135),
      sea = random.color(0 to 0, 0 to 60, 50 to 200),
      waterPercent = random.in(0.2, 0.8),
      cloudSeed = Some(random.nextLong()),
      heightSeed = Some(random.nextLong()),
      cloudSizeSeed = Some(random.nextLong()),
      darkenSeed = Some(random.nextLong()),
      cloudSizeFactor = random.in(4, 7),
      cloudSmallSkew = random.in(1, 5),
      cloudSizeSpeedFactor = random.in(0.1, 2.0),
      darkenSizeSpeedFactor = random.in(1, 5),
      darkenSizeFactor = random.in(2, 6),
      darkenSizeMin = random.in(2, 6),
      cloudMin = if (random.nextBoolean) 0.1 else 1.0
    )
  }
}
