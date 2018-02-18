package com.hwg

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import upickle.default._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

class Webservice(implicit system: ActorSystem) extends Directives {

  val systemMaster = system.actorOf(Props(new SystemMaster))

  implicit val materializer = ActorMaterializer()

  def route =
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
        // Scala-JS puts them in the root of the resource directory per default,
        // so that's where we pick them up
        path("frontend-launcher.js")(getFromResource("frontend-launcher.js")) ~
        path("frontend-fastopt.js")(getFromResource("frontend-fastopt.js")) ~
        path("frontend-opt.js")(getFromResource("frontend-opt.js")) ~
        path("frontend-fastopt.js.map")(getFromResource("frontend-fastopt.js.map")) ~
        path("websocket") {
          handleWebSocketMessages(websocketFlow())
        }
    } ~
      getFromResourceDirectory("web")

  def websocketFlow(): Flow[Message, Message, Any] = {
    val shipFlow = ShipFlow.create(system, systemMaster)

    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) =>
          Try(read[Protocol.Message](msg))
        // unpack incoming WS text messages...
        // This will lose (ignore) messages not received in one chunk (which is
        // unlikely because chat messages are small) but absolutely possible
        // FIXME: We need to handle TextMessage.Streamed as well.
      }
      .collect {
        case Success(msg) => msg
      }
      .via(shipFlow) // ... and route them through the chatFlow ...
      .map { msg: Protocol.Message ⇒
      TextMessage.Strict(write(msg)) // ... pack outgoing messages into WS JSON messages ...
    }
      .via(reportErrorsFlow) // ... then log any processing errors on stdin
  }

  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          println(s"WS stream failed with $cause")
        case _ => // ignore regular completion
      })
}
