package com.hwg

import com.hwg.models.Ship

object LocalMessages {
  case class ShipUpdate(id: Int, ship: Ship)
  case class ShipLeft(id: Int)
}
