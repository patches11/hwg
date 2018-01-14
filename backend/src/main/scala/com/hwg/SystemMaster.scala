package com.hwg

import java.time.Clock

import akka.actor.{Actor, ActorLogging}
import com.hwg.LocalMessages.{ShipLeft, ShipUpdate}
import com.hwg.models.Ship
import Protocol.State

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.collection.mutable

class SystemMaster extends Actor with ActorLogging {
  import context._

  val ships: mutable.Map[Int, Ship] = mutable.Map()
  val clock = Clock.systemUTC()
  var lastTick = clock.millis()

  val tickInterval = 50

  private case object Tick

  system.scheduler.schedule(tickInterval milliseconds, tickInterval milliseconds, self, Tick)

  override def receive: Receive = {
    case ShipUpdate(id, s) =>
      val ship = ships.getOrElse(id, Ship(0, 0, 0, 0, 0))

      ships.update(id, ship.updateCommands(s))
    case Tick =>
      val thisTime = clock.millis()
      val deltaTime = thisTime - lastTick
      ships.foreach { case (_, s) => println(s);s.tick(deltaTime) }

      system.eventStream.publish(State(
        thisTime,
        tickInterval,
        ships.toMap
      ))

      lastTick = thisTime
    case ShipLeft(id) =>
      ships.remove(id)
  }
}