package com.hwg

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message}
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import boopickle.Default._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class Webservice(implicit system: ActorSystem) extends Directives {

  val systemMaster = system.actorOf(Props(new SystemMaster))

  implicit val materializer = ActorMaterializer()

  def route =
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
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
        case BinaryMessage.Strict(bytes) =>
          Unpickle[Protocol.Message].tryFromBytes(bytes.toByteBuffer)
        case BinaryMessage.Streamed(dataStream) =>
          ???
      }
      .collect {
        case Success(msg) => msg
      }
      .via(shipFlow)
      .map { msg: Protocol.Message =>
        val pickled = Pickle.intoBytes(msg)
        BinaryMessage.Strict(ByteString.fromByteBuffer(pickled))
      }
      .via(reportErrorsFlow)
  }

  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          println(s"WS stream failed with $cause")
        case _ => // ignore regular completion
          println("WS Completed")
      })
}
