package com.hwg.models

import enumeratum.values._

sealed abstract class Manuever(val value: Long) extends LongEnumEntry

case object Manuever
  extends LongEnum[Manuever] {

  val values = findValues

  case object Nothing  extends Manuever(value = 1L)
  case object CW extends Manuever(value = 2L)
  case object CCW extends Manuever(value = 3L)
  case object Reverse extends Manuever(value = 4L)

}

