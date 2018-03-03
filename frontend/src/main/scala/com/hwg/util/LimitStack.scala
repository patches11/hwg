package com.hwg.util

import scala.scalajs.js

class LimitStack[T](limit: Int) {
  private val underlying = new js.Array[T](limit + 1)
  private var length = 0

  def push(elem: T): Option[T] = {
    underlying.push(elem)
    length = length + 1
    if (length > limit) {
      length = length - 1
      Some(underlying.shift)
    } else {
      None
    }
  }
}
