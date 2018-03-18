package com.hwg.util

object Optionally {
  implicit class OptionallyImpl[A](private val self: A) extends AnyVal {
    def optionally(b: Boolean)(func: (A) => A): A = {
      if (b) {
        func(self)
      } else {
        self
      }
    }
  }
}

