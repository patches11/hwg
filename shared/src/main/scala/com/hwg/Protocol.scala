package com.hwg

import com.hwg.models.Ship

object Protocol {
  sealed trait Message

  case class TimeMessage(sendTime: Long,
                  serverTime: Long,
                  receiveTime: Long) extends Message

  case class Dead(id: Long) extends Message

  case class ThisShip(ship: Ship) extends Message

  case class State(
                  id: Long, // UTC Millis
                  tickInterval: Long,
                  ships: Map[Int, Ship]
                  ) extends Message

  case class Initialized(id: Int) extends Message

  case class SendMessage(text: String) extends Message
  case class ReceiveMessage(who: String, time: Long, text: String) extends Message
}