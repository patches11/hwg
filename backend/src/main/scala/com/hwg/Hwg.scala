package com.hwg

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import slogging._

import scala.util.{Failure, Success}

object Hwg extends App with LazyLogging {
  LoggerConfig.factory = PrintLoggerFactory()

  LoggerConfig.level = LogLevel.DEBUG
  implicit val system = ActorSystem()
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = system.settings.config
  val interface = config.getString("app.interface")
  val port = config.getInt("app.port")

  val service = new Webservice

  val binding = Http().bindAndHandle(service.route, interface, port)

  binding.onComplete {
    case Success(binding) ⇒
      val localAddress = binding.localAddress
      logger.debug(s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(e) ⇒
      logger.error(s"Binding failed with ${e.getMessage}")
      system.terminate()
  }
}
