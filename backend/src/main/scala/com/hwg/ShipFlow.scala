package com.hwg

import java.time.Clock

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Flow
import com.hwg.LocalMessages.{ShipLeft, ShipUpdate}
import com.hwg.Protocol._

class ShipFlow(version: String) {
  private val bufferSize = 1000
  private val overflowStrategy = OverflowStrategy.dropHead
  private var id = 0

  def create(system: ActorSystem, systemMaster: ActorRef, chatMaster: ActorRef)(implicit actorRefFactory: ActorRefFactory, mat: akka.stream.Materializer): Flow[Protocol.Message, Protocol.Message, Any] = {
    ActorFlow.actorRef[Protocol.Message, Protocol.Message]({ out =>
      props(out, systemMaster, chatMaster)
    }, bufferSize, overflowStrategy)
  }

  def props(out: ActorRef, systemMaster: ActorRef, chatMaster: ActorRef): Props = {
    val props = Props(new ShipActor(out, id, systemMaster, chatMaster))
    id = id + 1
    props
  }

  private class ShipActor(var output: ActorRef, id: Int, systemMaster: ActorRef, chatMaster: ActorRef) extends Actor with ActorLogging {

    import context._

    private val clock = Clock.systemUTC()

    system.eventStream.subscribe(self, classOf[State])
    system.eventStream.subscribe(self, classOf[Dead])
    system.eventStream.subscribe(self, classOf[ReceiveMessage])


    override def preStart(): Unit = {
      super.preStart()
      output ! Initialized(id, version)
      output ! ReceiveMessage("System", clock.millis(), "Welcome to HWG")
    }

    def receive: Receive = {
      case t: TimeMessage =>
        output ! t.copy(serverTime = clock.millis())
      case ThisShip(ship) =>
        systemMaster ! ShipUpdate(id, ship)
      case SendMessage(text) =>
        chatMaster ! InternalMessage(id, text)
      case rm: ReceiveMessage =>
        output ! rm
      case su: State =>
        output ! su
      case dead: Dead =>
        output ! dead
    }

    override def postStop() = {
      log.info(s"Ship Actor $id closing")
      systemMaster ! ShipLeft(id)
    }
  }

}

