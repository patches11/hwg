package com.hwg.util.noise

object PerlinNoise extends Noise {

  val permutation = Array(151, 160, 137, 91, 90, 15,
  131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23,
  190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33,
  88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166,
  77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244,
  102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196,
  135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123,
  5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42,
  223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
  129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228,
  251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107,
  49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254,
  138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180)

  val p = permutation ++ permutation

  def noise(x0: Double, y0: Double, z0: Double): Double = {
    val X = Math.floor(x0).toInt & 255                  // FIND UNIT CUBE THAT
    val Y = Math.floor(y0).toInt & 255                  // CONTAINS POINT.
    val Z = Math.floor(z0).toInt & 255
    val x = x0 - Math.floor(x0)                                // FIND RELATIVE X,Y,Z
    val y = y0 - Math.floor(y0)                                // OF POINT IN CUBE.
    val z = z0 - Math.floor(z0)
    val u = this.fade(x)                                // COMPUTE FADE CURVES
    val v = this.fade(y)                                // FOR EACH OF X,Y,Z.
    val w = this.fade(z)
    val A = p(X) + Y
    val AA = p(A) + Z
    val AB = p(A + 1) + Z      // HASH COORDINATES OF
    val B = p(X + 1) + Y
    val BA = p(B) + Z
    val BB = p(B + 1) + Z      // THE 8 CUBE CORNERS,

    this.scale(this.lerp(w, this.lerp(v, this.lerp(u, this.grad(p(AA), x, y, z),  // AND ADD
      this.grad(p(BA), x - 1, y, z)), // BLENDED
      this.lerp(u, this.grad(p(AB), x, y - 1, z),  // RESULTS
      this.grad(p(BB), x - 1, y - 1, z))),// FROM  8
      this.lerp(v, this.lerp(u, this.grad(p(AA + 1), x, y, z - 1),  // CORNERS
      this.grad(p(BA + 1), x - 1, y, z - 1)), // OF CUBE
      this.lerp(u, this.grad(p(AB + 1), x, y - 1, z - 1),
      this.grad(p(BB + 1), x - 1, y - 1, z - 1)))))
  }

  def fade(t: Double): Double = { t * t * t * (t * (t * 6 - 15) + 10) }

  def lerp(t: Double, a: Double, b: Double): Double = { a + t * (b - a) }

  def grad(hash: Double, x: Double, y: Double, z: Double): Double = {
    val h = hash.toInt & 15                      // CONVERT LO 4 BITS OF HASH CODE
    val u = if (h < 8) x else y               // INTO 12 GRADIENT DIRECTIONS.
    val v = h match {
      case hh if hh < 4 => y
      case hh if hh == 12 || hh == 14 => x
      case _ => z
    }
    (if ((h & 1) == 0) u else -u) + (if ((h & 2) == 0) v else -v)
  }

  def scale(n: Double): Double = { (1 + n) / 2; }
}
