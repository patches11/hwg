package com.hwg

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message}
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Partition}
import akka.stream.{ActorMaterializer, FlowShape}
import akka.util.ByteString
import boopickle.Default._
import slogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

class Webservice(implicit system: ActorSystem) extends Directives with LazyLogging {

  val systemMaster = system.actorOf(Props(new SystemMaster))
  val chatMaster   = system.actorOf(Props(new SystemChat))

  implicit val materializer = ActorMaterializer()

  private val streamedFlow = Flow[Message]
    .collect {
      case BinaryMessage.Streamed(dataStream) =>
        dataStream
          .completionTimeout(2 seconds)
          .runFold(ByteString.empty)(_ ++ _)
    }
    .mapAsync(parallelism = 3)(identity)

  private val strictFlow = Flow[Message]
    .collect {
      case BinaryMessage.Strict(bytes) =>
        bytes
    }

  private val splitFlow = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val split = builder.add(Partition(2, portMapper))
    val merge = builder.add(Merge[ByteString](2))

    split.out(0) ~> streamedFlow ~> merge.in(0)
    split.out(1) ~> strictFlow ~> merge.in(1)

    FlowShape(split.in, merge.out)
  })

  private def portMapper(value: Message): Int = {
    value match {
      case BinaryMessage.Streamed(_) => 0
      case BinaryMessage.Strict(_) => 1
      case _ => 1
    }
  }

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
    val shipFlow = ShipFlow.create(system, systemMaster, chatMaster)

    splitFlow
      .map { bytes =>
        Unpickle[Protocol.Message].tryFromBytes(bytes.toByteBuffer)
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
          logger.warn(s"WS stream failed with $cause")
        case _ => // ignore regular completion
          logger.debug("WS Completed")
      })
}
