package com.hwg

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.util.Failure

import scala.concurrent.ExecutionContext.Implicits.global

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
          handleWebSocketMessages(flow())
        }
    } ~
      getFromResourceDirectory("web")

  def flow(): Flow[Message, Message, Any] = {

    val in = Sink.ignore

    val out = Source.empty[Message]

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
