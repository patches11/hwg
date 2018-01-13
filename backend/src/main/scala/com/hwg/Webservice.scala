package com.hwg

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.hwg.models.Ship
import shared.Protocol
import shared.Protocol.ThisShip
import upickle.default._

import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Webservice(implicit system: ActorSystem) extends Directives {

  def route =
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
        // Scala-JS puts them in the root of the resource directory per default,
        // so that's where we pick them up
        path("frontend-launcher.js")(getFromResource("frontend-launcher.js")) ~
        path("frontend-fastopt.js")(getFromResource("frontend-fastopt.js")) ~
        path("frontend-fastopt.js.map")(getFromResource("frontend-fastopt.js.map")) ~
        path("websocket") {
          handleWebSocketMessages(websocketFlow())
        }
    } ~
      getFromResourceDirectory("web")

  def websocketFlow(): Flow[Message, Message, Any] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) ⇒ msg // unpack incoming WS text messages...
        // This will lose (ignore) messages not received in one chunk (which is
        // unlikely because chat messages are small) but absolutely possible
        // FIXME: We need to handle TextMessage.Streamed as well.
      }
      .via(dataFlow()) // ... and route them through the chatFlow ...
      .map {
      case msg: Protocol.Message ⇒
        TextMessage.Strict(write(msg)) // ... pack outgoing messages into WS JSON messages ...
    }
      .via(reportErrorsFlow) // ... then log any processing errors on stdin

  def dataFlow(): Flow[String, Protocol.Message, Any] = {
    val in = Sink.foreach[String](println)

    // The counter-part which is a source that will create a target ActorRef per
    // materialization where the chatActor will send its messages to.
    // This source will only buffer one element and will fail if the client doesn't read
    // messages fast enough.
    val out = Source.tick[Protocol.Message](1 second, 1 second, ThisShip(Ship(0, 0, 0, 0, 0)))

    Flow.fromSinkAndSource(in, out)
  }

  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          println(s"WS stream failed with $cause")
        case _ => // ignore regular completion
      })
}
