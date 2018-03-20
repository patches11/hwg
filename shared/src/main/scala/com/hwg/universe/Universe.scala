package com.hwg.universe

import scala.util.Random

object Universe {
  import com.hwg.util.RandomUtils._

  val max = 5

  val systemsIds: Seq[Long] =
    Seq(
      31687L
    )

  val systems: Seq[SystemConfig] = systemsIds.map { id =>
    val rand = new Random(id)
    SystemConfig(
      id,
      (1 to rand.in(1 to max)).map { _ =>
        PlanetConfig(
          (rand.in(-10, 10), rand.in(-10, 10)),
          rand.in(1, 10),
          rand.in(2, 6),
          rand.in(0, 2 * Math.PI)
        )
      }
    )
  }
}

case class SystemConfig(
                         seed: Long,
                         planets: Seq[PlanetConfig]
                       ) {
  def dir = s"img/gen/systems/$seed"
  def background = dir + "/background.png"
  def tex(index: Int) = dir + s"/$index.png"
}

case class PlanetConfig(
                       center: (Double, Double),
                       radius: Double,
                       size: Double,
                       start: Double
                       )