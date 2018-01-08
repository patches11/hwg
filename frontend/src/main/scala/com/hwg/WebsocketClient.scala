package com.hwg

import monix.reactive.Observable
import org.scalajs.dom
import org.scalajs.dom.raw.{Event, MessageEvent, WebSocket}
import monix.reactive.subjects.PublishSubject
import shared.Protocol.Message
import upickle.default._

class WebsocketClient(val limit: Int = 1000) {

  private var active = false

  private val websocket = new WebSocket(getWebsocketUri)

  private val observable = PublishSubject[Message]()

  websocket.onopen = { (event: Event) =>
    active = true
  }

  websocket.onerror = { (event: Event) =>
    active = false
  }

  websocket.onclose = { (event: Event) =>
    active = false
  }

  websocket.onmessage = { (event: MessageEvent) =>
    val wsMsg = read[Message](event.data.toString)

    observable.onNext(wsMsg)
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
