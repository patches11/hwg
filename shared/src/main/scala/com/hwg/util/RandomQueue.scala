package com.hwg.util

import scala.collection.mutable
import scala.util.Random

class RandomQueue[T](val random: Random) {

  val base: mutable.ArrayBuffer[T] = mutable.ArrayBuffer()

  def push(item: T): Unit = {
    base.append(item)
  }

  // TODO: Maybe return option T
  def pop(): T = {
    base.remove(random.nextInt())
  }

  def isEmpty: Boolean = {
    base.isEmpty
  }

  def size: Int = {
    base.size
  }
}
