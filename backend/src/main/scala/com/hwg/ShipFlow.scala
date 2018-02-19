package com.hwg

import java.time.Clock

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, ActorSystem, Props}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.Flow
import com.hwg.LocalMessages.{ShipLeft, ShipUpdate}
import com.hwg.Protocol._

object ShipFlow {
  val bufferSize = 1000
  val overflowStrategy = OverflowStrategy.dropHead
  var id = 0

  def create(system: ActorSystem, systemMaster: ActorRef)(implicit actorRefFactory: ActorRefFactory, mat: akka.stream.Materializer): Flow[Protocol.Message, Protocol.Message, Any] = {
    ActorFlow.actorRef[Protocol.Message, Protocol.Message]({ out =>
      props(out, systemMaster)
    }, bufferSize, overflowStrategy)
  }

  def props(out: ActorRef, systemMaster: ActorRef): Props = {
    val props = Props(new ShipActor(out, id, systemMaster))
    id = id + 1
    props
  }

  private class ShipActor(output: ActorRef, id: Int, systemMaster: ActorRef) extends Actor with ActorLogging {

    import context._

    private val clock = Clock.systemUTC()

    system.eventStream.subscribe(self, classOf[State])
    system.eventStream.subscribe(self, classOf[Dead])

    output ! Initialized(id)

    def receive: Receive = {
      case t: TimeMessage =>
        output ! t.copy(serverTime = clock.millis())
      case ThisShip(ship) =>
        systemMaster ! ShipUpdate(id, ship)
      case su: State =>
        output ! su
      case dead: Dead =>
        output ! dead
    }

    override def postStop() = {
      systemMaster ! ShipLeft(id)
    }
  }

}

