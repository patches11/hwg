package com.hwg

import java.time.Clock

import akka.actor.{Actor, ActorLogging}
import com.hwg.LocalMessages.{ShipLeft, ShipUpdate}
import com.hwg.models.Ship
import Protocol.{Dead, State}
import com.hwg.util.MathExt

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
      val ship = ships.getOrElse(id, Ship())

      ships.update(id, ship.updateCommands(s))
    case Tick =>
      val thisTime = clock.millis()
      val deltaTime = thisTime - lastTick

      ships.foreach { case (id, s) =>
        s.tick(deltaTime)
        s.projectiles.foreach { p =>
          ships.foreach { case (idi, si) =>
            if (id != idi && MathExt.distance(p.x, p.y, si.x, si.y) < 50) {
              system.eventStream.publish(Dead(idi))
              ships.remove(idi)
            }
          }
          p.tick(deltaTime)
        }
        s.projectiles = s.projectiles.filter(_.lifetime > Duration.Zero)
      }

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