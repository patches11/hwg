package com.hwg.util

import scala.scalajs.js.typedarray.{Float32Array, Uint16Array}

object TypedArrayConverters {
  implicit object Float32Array {
    def apply[T: Numeric](xs: Iterable[T]): Float32Array = {
      val array = scalajs.js.Array[T](xs.toSeq:_*)
      new Float32Array(array)
    }
  }

  implicit object Uint16Array {
    def apply[T: Numeric](xs: Iterable[T]): Uint16Array = {
      val array = scalajs.js.Array[T](xs.toSeq:_*)
      new Float32Array(array)
    }
  }
}
