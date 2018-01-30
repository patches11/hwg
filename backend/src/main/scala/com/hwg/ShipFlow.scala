package com.hwg

import java.time.Clock

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.hwg.LocalMessages.{ShipLeft, ShipUpdate}
import Protocol._
import upickle.default._

import scala.util.{Success, Try}

trait ShipFlow {
  def flow(): Flow[String, Protocol.Message, Any]
}

object ShipFlow {
  var id = 0

  val bufferSize = 1000

  def create(system: ActorSystem, systemMaster: ActorRef): ShipFlow = {
    val actor = system.actorOf(Props(new ShipActor(systemMaster)))

    val sink = Sink.actorRef[Protocol.Message](actor, {
      val localId = id
      actor ! ShipLeft(localId)
    })

    new ShipFlow {
      def flow(): Flow[String, Protocol.Message, Any] = {
        val in =
          Flow[String]
            .map(s => Try(read[Protocol.Message](s)))
            .collect {
              case Success(m) => m
            }
            .to(sink)

        // The counter-part which is a source that will create a target ActorRef per
        // materialization where the chatActor will send its messages to.
        // This source will only buffer one element and will fail if the client doesn't read
        // messages fast enough.

        val out = Source.actorRef[Protocol.Message](bufferSize, OverflowStrategy.dropHead).mapMaterializedValue(ref => actor ! Init(ref, id))
        id = id + 1

        Flow.fromSinkAndSource(in, out)
      }
    }
  }

  private case class Init(ref: ActorRef, id: Int)

  private class ShipActor(systemMaster: ActorRef) extends Actor with ActorLogging {
    import context._

    private val clock = Clock.systemUTC()
    var output: ActorRef = _
    var id: Int = _

    system.eventStream.subscribe(self, classOf[State])
    system.eventStream.subscribe(self, classOf[Dead])

    def receive: Receive = {
      case Init(a, i) =>
        output = a
        id = i
        output ! Initialized(i)
        become(active)
    }

    def active: Receive = {
      case t: TimeMessage =>
        output ! t.copy(serverTime = clock.millis())
      case ThisShip(ship) =>
        systemMaster ! ShipUpdate(id, ship)
      case su: State =>
        output ! su
      case dead: Dead =>
        output ! dead
    }
  }
}

