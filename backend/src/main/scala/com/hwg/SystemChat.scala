package com.hwg

import java.time.Clock

import akka.actor.{Actor, ActorLogging}
import com.hwg.Protocol.ReceiveMessage

import scala.concurrent.duration._

class SystemChat extends Actor with ActorLogging {
  import context._

  private val clock = Clock.systemUTC()

  override def receive: Receive = {
    case InternalMessage(id, text) =>
      system.eventStream.publish(ReceiveMessage(id.toString, clock.millis(), text))
  }
}

case class InternalMessage(id: Int, text: String)