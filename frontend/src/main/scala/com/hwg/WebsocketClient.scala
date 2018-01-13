package com.hwg

import monix.execution.{Ack, Cancelable}
import monix.execution.cancelables.SingleAssignmentCancelable
import monix.reactive.Observable
import monix.reactive.OverflowStrategy.DropOld
import org.scalajs.dom
import org.scalajs.dom.raw.{Event, MessageEvent, WebSocket}
import monix.reactive.subjects.PublishSubject
import shared.Protocol.Message
import upickle.default._

import scala.scalajs.js

class WebsocketClient(val limit: Int = 1000) {

  import monix.execution.Scheduler.Implicits.global

  private var active = false

  private val websocket = new WebSocket(getWebsocketUri)

  PublishSubject[Message]()

  websocket.onopen = { (event: Event) =>
    active = true
  }

  websocket.onerror = { (event: Event) =>
    active = false
  }

  websocket.onclose = { (event: Event) =>
    active = false
  }

  private val observable: Observable[Message] = Observable.create(DropOld(limit)) { subscriber =>
    val c = SingleAssignmentCancelable()
    // Forced conversion, otherwise canceling will not work!
    val f: js.Function1[MessageEvent, Ack] = (e: MessageEvent) => {
      val wsMsg = read[Message](e.data.toString)

      subscriber.onNext(wsMsg).syncOnStopOrFailure((_) => c.cancel())
    }

    websocket.addEventListener("message", f)
    c := Cancelable(() => {
      websocket.removeEventListener("message", f)
    })
  }

  def getObservable: Observable[Message] = observable

  def alive: Boolean = this.active

  def send(validMessage: Message): Unit = {
    websocket.send(write(validMessage))
  }

  def getWebsocketUri: String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${dom.document.location.host}/websocket"
  }

}
