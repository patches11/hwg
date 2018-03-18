package com.hwg.util.noise

import scala.util.Random

class SimplexNoise(val random: Random) extends Noise {
  val grad3 = Array((1,1,0),(-1,1,0),(1,-1,0),(-1,-1,0),
    (1,0,1),(-1,0,1),(1,0,-1),(-1,0,-1),
    (0,1,1),(0,-1,1),(0,1,-1),(0,-1,-1))

  val simplex = Array(
  (0,1,2,3),(0,1,3,2),(0,0,0,0),(0,2,3,1),(0,0,0,0),(0,0,0,0),(0,0,0,0),(1,2,3,0),
  (0,2,1,3),(0,0,0,0),(0,3,1,2),(0,3,2,1),(0,0,0,0),(0,0,0,0),(0,0,0,0),(1,3,2,0),
  (0,0,0,0),(0,0,0,0),(0,0,0,0),(0,0,0,0),(0,0,0,0),(0,0,0,0),(0,0,0,0),(0,0,0,0),
  (1,2,0,3),(0,0,0,0),(1,3,0,2),(0,0,0,0),(0,0,0,0),(0,0,0,0),(2,3,0,1),(2,3,1,0),
  (1,0,2,3),(1,0,3,2),(0,0,0,0),(0,0,0,0),(0,0,0,0),(2,0,3,1),(0,0,0,0),(2,1,3,0),
  (0,0,0,0),(0,0,0,0),(0,0,0,0),(0,0,0,0),(0,0,0,0),(0,0,0,0),(0,0,0,0),(0,0,0,0),
  (2,0,1,3),(0,0,0,0),(0,0,0,0),(0,0,0,0),(3,0,1,2),(3,0,2,1),(0,0,0,0),(3,1,2,0),
  (2,1,0,3),(0,0,0,0),(0,0,0,0),(0,0,0,0),(3,1,0,2),(0,0,0,0),(3,2,0,1),(3,2,1,0)
  )

  val perm = {
    val p = (0 until 256).map(_ => random.nextInt(256)).toArray
    (0 until 512).map(i => p(i & 255))
  }

  def dot(g: (Int, Int, Int), x: Double, y: Double): Double = {
    g._1*x + g._2*y
  }

  def dot(g: (Int, Int, Int), x: Double, y: Double, z: Double): Double = {
    g._1 * x + g._2 * y + g._3 * z
  }

  def noise2d(xin: Double, yin: Double): Double = {
    // n0, n1, n2 // Noise contributions from the three corners
    // Skew the input space to determine which simplex cell we're in
    val F2 = 0.5*(Math.sqrt(3.0)-1.0)
    val s = (xin+yin)*F2 // Hairy factor for 2D
    val i = Math.floor(xin+s).toInt
    val j = Math.floor(yin+s).toInt
    val G2 = (3.0-Math.sqrt(3.0))/6.0
    val t = (i+j)*G2
    val X0 = i-t // Unskew the cell origin back to (x,y) space
    val Y0 = j-t
    val x0 = xin-X0 // The x,y distances from the cell origin
    val y0 = yin-Y0
    // For the 2D case, the simplex shape is an equilateral triangle.
    // Determine which simplex we are in.

    // Offsets for second (middle) corner of simplex in (i,j) coords
    val (i1, j1) = if (x0>y0) { (1, 0) } else { (0, 1) } // lower triangle, XY order: (0,0)->(1,0)->(1,1) upper triangle, YX order: (0,0)->(0,1)->(1,1)


    // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
    // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y), where
    // c = (3-sqrt(3))/6
    val x1 = x0 - i1 + G2 // Offsets for middle corner in (x,y) unskewed coords
    val y1 = y0 - j1 + G2
    val x2 = x0 - 1.0 + 2.0 * G2 // Offsets for last corner in (x,y) unskewed coords
    val y2 = y0 - 1.0 + 2.0 * G2
    // Work out the hashed gradient indices of the three simplex corners

    val ii = i & 255
    val jj = j & 255
    val gi0 = this.perm(ii+this.perm(jj)) % 12
    val gi1 = this.perm(ii+i1+this.perm(jj+j1)) % 12
    val gi2 = this.perm(ii+1+this.perm(jj+1)) % 12
    // Calculate the contribution from the three corners
    var t0 = 0.5 - x0*x0-y0*y0
    val n0 = if (t0<0) 0.0
    else {
      t0 = t0 * t0
      t0 * t0 * this.dot(this.grad3(gi0), x0, y0)  // (x,y) of grad3 used for 2D gradient
    }

    var t1 = 0.5 - x1*x1-y1*y1
    val n1 = if(t1<0) 0.0
    else {
      t1 = t1 * t1
      t1 * t1 * this.dot(this.grad3(gi1), x1, y1)
    }

    var t2 = 0.5 - x2*x2-y2*y2
    val n2 = if(t2<0) 0.0
    else {
      t2 = t2 * t2
      t2 * t2 * this.dot(this.grad3(gi2), x2, y2)
    }
    // Add contributions from each corner to get the final noise value.
    // The result is scaled to return values in the interval [-1,1].
    70.0 * (n0 + n1 + n2)
  }

  def noise(xin: Double, yin: Double, zin: Double): Double = {
    // n0, n1, n2, n3 // Noise contributions from the four corners
    // Skew the input space to determine which simplex cell we're in
    val F3 = 1.0/3.0
    val s = (xin+yin+zin)*F3 // Very nice and simple skew factor for 3D
    val i = Math.floor(xin+s).toInt
    val j = Math.floor(yin+s).toInt
    val k = Math.floor(zin+s).toInt
    val G3 = 1.0/6.0 // Very nice and simple unskew factor, too
    val t = (i+j+k)*G3
    val X0 = i-t // Unskew the cell origin back to (x,y,z) space
    val Y0 = j-t
    val Z0 = k-t
    val x0 = xin-X0 // The x,y,z distances from the cell origin
    val y0 = yin-Y0
    val z0 = zin-Z0
    // For the 3D case, the simplex shape is a slightly irregular tetrahedron.
    // Determine which simplex we are in.
    // i1, j1, k1 // Offsets for second corner of simplex in (i,j,k) coords
    // i2, j2, k2 // Offsets for third corner of simplex in (i,j,k) coords
    val (i1, j1, k1, i2, j2, k2) = if (x0 >= y0) {
      if(y0>=z0)
      { (1, 0, 0, 1, 1, 0) } // X Y Z order
      else if(x0>=z0) { (1, 0, 0, 1, 0, 1) } // X Z Y order
      else { (0, 0, 1, 1, 0, 1) } // Z X Y order
    }
    else { // x0<y0
      if(y0<z0) { (0, 0, 1, 0, 1, 1) } // Z Y X order
      else if(x0<z0) { (0, 1, 0, 0, 1, 1) } // Y Z X order
      else { (0, 1, 0, 1, 1, 0) } // Y X Z order
    }
    // A step of (1,0,0) in (i,j,k) means a step of (1-c,-c,-c) in (x,y,z),
    // a step of (0,1,0) in (i,j,k) means a step of (-c,1-c,-c) in (x,y,z), and
    // a step of (0,0,1) in (i,j,k) means a step of (-c,-c,1-c) in (x,y,z), where
    // c = 1/6.
    val x1 = x0 - i1 + G3 // Offsets for second corner in (x,y,z) coords
    val y1 = y0 - j1 + G3
    val z1 = z0 - k1 + G3
    val x2 = x0 - i2 + 2.0*G3 // Offsets for third corner in (x,y,z) coords
    val y2 = y0 - j2 + 2.0*G3
    val z2 = z0 - k2 + 2.0*G3
    val x3 = x0 - 1.0 + 3.0*G3 // Offsets for last corner in (x,y,z) coords
    val y3 = y0 - 1.0 + 3.0*G3
    val z3 = z0 - 1.0 + 3.0*G3
    // Work out the hashed gradient indices of the four simplex corners
    val ii = i & 255
    val jj = j & 255
    val kk = k & 255
    val gi0 = this.perm(ii+this.perm(jj+this.perm(kk))) % 12
    val gi1 = this.perm(ii+i1+this.perm(jj+j1+this.perm(kk+k1))) % 12
    val gi2 = this.perm(ii+i2+this.perm(jj+j2+this.perm(kk+k2))) % 12
    val gi3 = this.perm(ii+1+this.perm(jj+1+this.perm(kk+1))) % 12
    // Calculate the contribution from the four corners

    var t0 = 0.6 - x0*x0 - y0*y0 - z0*z0
    val n0 = if(t0<0) 0.0
    else {
      t0 = t0 * t0
      t0 * t0 * this.dot(this.grad3(gi0), x0, y0, z0)
    }

    var t1 = 0.6 - x1*x1 - y1*y1 - z1*z1
    val n1 = if (t1<0) 0.0
    else {
      t1 *= t1
      t1 * t1 * this.dot(this.grad3(gi1), x1, y1, z1)
    }

    var t2 = 0.6 - x2*x2 - y2*y2 - z2*z2
    val n2 = if (t2<0) 0.0
    else {
      t2 *= t2
      t2 * t2 * this.dot(this.grad3(gi2), x2, y2, z2)
    }

    var t3 = 0.6 - x3*x3 - y3*y3 - z3*z3
    val n3 = if (t3<0) 0.0
    else {
      t3 *= t3
      t3 * t3 * this.dot(this.grad3(gi3), x3, y3, z3)
    }
    // Add contributions from each corner to get the final noise value.
    // The result is scaled to stay just inside [-1,1]
    32.0*(n0 + n1 + n2 + n3)
  }
}