package com.hwg.util

import scryetek.vecmath.{Mat4, Vec3}

import scala.scalajs.js

object VecmathConverters {

  implicit class Vec3Converter(vec3: Vec3) {
    def toJsArray: js.Array[Double] = js.Array[Double](vec3.x, vec3.y, vec3.z)
  }

  implicit class Mat4Converter(mat4: Mat4) {
    def toJsArray: js.Array[Double] = js.Array(
      mat4.m00, mat4.m01, mat4.m02, mat4.m03,
      mat4.m10, mat4.m11, mat4.m12, mat4.m13,
      mat4.m20, mat4.m21, mat4.m22, mat4.m23,
      mat4.m30, mat4.m31, mat4.m32, mat4.m33
    )

    def toJsArrayT: js.Array[Double] = js.Array(
      mat4.m00, mat4.m10, mat4.m20, mat4.m30,
      mat4.m01, mat4.m11, mat4.m21, mat4.m31,
      mat4.m02, mat4.m12, mat4.m22, mat4.m32,
      mat4.m03, mat4.m13, mat4.m23, mat4.m33
    )

    def dup: Mat4 = {
      new Mat4().set(mat4)
    }
  }

}
