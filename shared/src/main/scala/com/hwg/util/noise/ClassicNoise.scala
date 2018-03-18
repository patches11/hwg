package com.hwg.util.noise

import scala.util.Random

class ClassicNoise(val random: Random) extends Noise {
  val grad3 = Array((1, 1, 0), (-1, 1, 0), (1, -1, 0), (-1, -1, 0),
    (1, 0, 1), (-1, 0, 1), (1, 0, -1), (-1, 0, -1),
    (0, 1, 1), (0, -1, 1), (0, 1, -1), (0, -1, -1))

  private val perm = {
    val p = (0 until 256).map(_ => random.nextInt(256)).toArray
    (0 until 512).map(i => p(i & 255))
  }

  def noise(xx: Double, yy: Double, zz: Double): Double = {
    // Find unit grid cell containing point
    var X = xx.toInt
    var Y = yy.toInt
    var Z = zz.toInt

    // Get relative xyz coordinates of point within that cell
    val x = xx - X
    val y = yy - Y
    val z = zz - Z

    // Wrap the integer cells at 255 (smaller integer period can be introduced here)
    X = X & 255
    Y = Y & 255
    Z = Z & 255

    // Calculate a set of eight hashed gradient indices
    val gi000 = this.perm(X + this.perm(Y + this.perm(Z))) % 12
    val gi001 = this.perm(X + this.perm(Y + this.perm(Z + 1))) % 12
    val gi010 = this.perm(X + this.perm(Y + 1 + this.perm(Z))) % 12
    val gi011 = this.perm(X + this.perm(Y + 1 + this.perm(Z + 1))) % 12
    val gi100 = this.perm(X + 1 + this.perm(Y + this.perm(Z))) % 12
    val gi101 = this.perm(X + 1 + this.perm(Y + this.perm(Z + 1))) % 12
    val gi110 = this.perm(X + 1 + this.perm(Y + 1 + this.perm(Z))) % 12
    val gi111 = this.perm(X + 1 + this.perm(Y + 1 + this.perm(Z + 1))) % 12

    // The gradients of each corner are now:
    // g000 = grad3(gi000)
    // g001 = grad3(gi001)
    // g010 = grad3(gi010)
    // g011 = grad3(gi011)
    // g100 = grad3(gi100)
    // g101 = grad3(gi101)
    // g110 = grad3(gi110)
    // g111 = grad3(gi111)
    // Calculate noise contributions from each of the eight corners
    val n000 = this.dot(this.grad3(gi000), x, y, z)
    val n100 = this.dot(this.grad3(gi100), x - 1, y, z)
    val n010 = this.dot(this.grad3(gi010), x, y - 1, z)
    val n110 = this.dot(this.grad3(gi110), x - 1, y - 1, z)
    val n001 = this.dot(this.grad3(gi001), x, y, z - 1)
    val n101 = this.dot(this.grad3(gi101), x - 1, y, z - 1)
    val n011 = this.dot(this.grad3(gi011), x, y - 1, z - 1)
    val n111 = this.dot(this.grad3(gi111), x - 1, y - 1, z - 1)
    // Compute the fade curve value for each of x, y, z
    val u = this.fade(x)
    val v = this.fade(y)
    val w = this.fade(z)
    // Interpolate along x the contributions from each of the corners
    val nx00 = this.mix(n000, n100, u)
    val nx01 = this.mix(n001, n101, u)
    val nx10 = this.mix(n010, n110, u)
    val nx11 = this.mix(n011, n111, u)
    // Interpolate the four results along y
    val nxy0 = this.mix(nx00, nx10, v)
    val nxy1 = this.mix(nx01, nx11, v)
    // Interpolate the two last results along z
    val nxyz = this.mix(nxy0, nxy1, w)

    nxyz
  }

  def dot(g: (Int, Int, Int), x: Double, y: Double, z: Double): Double = {
    g._1 * x + g._2 * y + g._3 * z
  }

  def mix(a: Double, b: Double, t: Double): Double = {
    (1.0 - t) * a + t * b
  }

  def fade(t: Double): Double = {
    t * t * t * (t * (t * 6.0 - 15.0) + 10.0)
  }
}