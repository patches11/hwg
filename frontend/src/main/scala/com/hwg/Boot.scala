package com.hwg

import monix.execution.cancelables.SingleAssignmentCancelable
import monix.execution.{Ack, Cancelable}
import monix.reactive.Observable
import monix.reactive.OverflowStrategy.DropOld
import org.scalajs.dom
import org.scalajs.dom.raw.{WheelEvent, Window}
import org.scalajs.dom.{KeyboardEvent, TouchEvent, html, raw}

import scala.scalajs.js
import slogging._
object Boot {

  import monix.execution.Scheduler.Implicits.global

  private val eventLimit = 10000
  private val canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]

  def main(_args: Array[String]): Unit = {
    LoggerConfig.factory = ConsoleLoggerFactory()

    val gl: raw.WebGLRenderingContext = canvas.getContext("webgl", {}).asInstanceOf[raw.WebGLRenderingContext]

    dom.window.addEventListener("boot", _ => {
      val page = dom.document.getElementById("page").asInstanceOf[html.Div]
      val ua = dom.window.navigator.userAgent
      val iphone = ua.indexOf("iPhone") > 0 || ua.indexOf("iPod") > 0
      val ipad = ua.indexOf("iPad") > 0
      val ios = iphone || ipad
      val fullscreen = false // dom.window.navigator.standalone
      if (ios) {
        var height = dom.document.documentElement.clientHeight;
        if (iphone && !fullscreen) {
          height += 60
        }
        page.style.height = s"${height}px"
        dom.window.scrollTo(0, 1)
      }
    })

    val keyboardEvents = keyboardEventListener().groupBy(_.keyCode).map(_.distinctUntilChangedByKey(_.`type`)).merge

    val touchEvents = touchEventListener()

    val wheelEvents = wheelEventListener

    new HwgApplication(gl, keyboardEvents, wheelEvents, touchEvents)
  }

  def keyboardEventListener(): Observable[KeyboardEvent] = {
    Observable.create(DropOld(eventLimit)) { subscriber =>
      val c = SingleAssignmentCancelable()
      // Forced conversion, otherwise canceling will not work!
      val f: js.Function1[KeyboardEvent, Ack] = (e: KeyboardEvent) =>
        subscriber.onNext(e).syncOnStopOrFailure(_ => c.cancel())

      dom.document.addEventListener("keydown", f)
      dom.document.addEventListener("keyup", f)
      c := Cancelable(() => {
        dom.document.removeEventListener("keydown", f)
        dom.document.removeEventListener("keyup", f)
      })
    }
  }

  def touchEventListener(): Observable[TouchEvent] = {
    Observable.create(DropOld(eventLimit)) { subscriber =>
      val c = SingleAssignmentCancelable()
      // Forced conversion, otherwise canceling will not work!
      val f: js.Function1[TouchEvent, Ack] = (e: TouchEvent) =>
        subscriber.onNext(e).syncOnStopOrFailure(_ => c.cancel())

      dom.document.addEventListener("touchstart", f)
      dom.document.addEventListener("touchend", f)
      dom.document.addEventListener("touchmove", f)
      c := Cancelable(() => {
        dom.document.removeEventListener("touchstart", f)
        dom.document.removeEventListener("touchend", f)
        dom.document.removeEventListener("touchmove", f)
      })
    }
  }

  def wheelEventListener: Observable[WheelEvent] = {
    Observable.create(DropOld(eventLimit)) { subscriber =>
      val c = SingleAssignmentCancelable()
      // Forced conversion, otherwise canceling will not work!
      val f: js.Function1[WheelEvent, Ack] = (e: WheelEvent) =>
        subscriber.onNext(e).syncOnStopOrFailure(_ => c.cancel())

      dom.document.addEventListener("wheel", f)
      c := Cancelable(() => {
        dom.document.removeEventListener("wheel", f)
      })
    }
  }
}
