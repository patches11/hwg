package com.hwg

import java.nio.ByteBuffer

import boopickle.Default._
import com.hwg.Protocol.{Message, TimeMessage}
import monix.execution.cancelables.SingleAssignmentCancelable
import monix.execution.{Ack, Cancelable}
import monix.reactive.Observable
import monix.reactive.OverflowStrategy.DropOld
import monix.reactive.subjects.PublishSubject
import org.scalajs.dom
import org.scalajs.dom.raw.{Event, MessageEvent, WebSocket}
import slogging.LazyLogging

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.typedarray.TypedArrayBufferOps._
import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer}

class WebsocketClient(val limit: Int = 1000) extends LazyLogging {

  import monix.execution.Scheduler.Implicits.global

  private val websocket = new WebSocket(getWebsocketUri)
  websocket.binaryType = "arraybuffer"

  var rcount = 0
  var scount = 0

  private val o: Observable[Message] = Observable.create(DropOld(limit)) { subscriber =>
    val c = SingleAssignmentCancelable()
    // Forced conversion, otherwise canceling will not work!
    val f: js.Function1[MessageEvent, Ack] = (e: MessageEvent) => {
      e.data match {
        case buff: ArrayBuffer =>
          val bytes: ByteBuffer = TypedArrayBuffer.wrap(buff)
          val wsMsg = Unpickle[Message].fromBytes(bytes) match {
            case t: TimeMessage =>
              t.copy(receiveTime = new Date().getTime.toLong)
            case a => a
          }
          rcount = rcount + 1
          if (rcount % 60 == 0)
            logger.info(wsMsg.toString)
          subscriber.onNext(wsMsg).syncOnStopOrFailure((_) => c.cancel())
      }
    }

    websocket.addEventListener("message", f)
    c := Cancelable(() => {
      websocket.removeEventListener("message", f)
    })
  }

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
  private val observable = o.share
  private var active = false

  def getObservable: Observable[Message] = observable

  def alive: Boolean = this.active

  implicit def bytes2message(data: ByteBuffer): ArrayBuffer = {
    if (data.hasTypedArray()) {
      // get relevant part of the underlying typed array
      data.typedArray().subarray(data.position, data.limit).buffer
    } else {
      // fall back to copying the data
      val tempBuffer = ByteBuffer.allocateDirect(data.remaining)
      val origPosition = data.position
      tempBuffer.put(data)
      data.position(origPosition)
      tempBuffer.typedArray().buffer
    }
  }

  def send(validMessage: Message): Unit = {
    scount = scount + 1
    if (scount % 60 == 0)
      logger.info(validMessage.toString)
    val data = Pickle.intoBytes(validMessage)
    websocket.send(data)
  }

  def getWebsocketUri: String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${dom.document.location.host}/websocket"
  }

}
