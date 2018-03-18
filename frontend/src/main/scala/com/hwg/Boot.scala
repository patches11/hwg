package com.hwg

import monix.execution.cancelables.SingleAssignmentCancelable
import monix.execution.{Ack, Cancelable}
import monix.reactive.Observable
import monix.reactive.OverflowStrategy.DropOld
import org.scalajs.dom
import org.scalajs.dom.raw.WheelEvent
import org.scalajs.dom.{KeyboardEvent, html, raw}

import scala.scalajs.js
import slogging._
object Boot extends js.JSApp {

  import monix.execution.Scheduler.Implicits.global

  private val eventLimit = 10000
  private val canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]

  def main(): Unit = {
    LoggerConfig.factory = ConsoleLoggerFactory()

    val gl: raw.WebGLRenderingContext = canvas.getContext("webgl", {}).asInstanceOf[raw.WebGLRenderingContext]

    val keyboardEvents = keyboardEventListener().groupBy(_.keyCode).map(_.distinctUntilChangedByKey(_.`type`)).merge

    val wheelEvents = wheelEventListener

    new HwgApplication(gl, keyboardEvents, wheelEvents)
  }

  def keyboardEventListener(): Observable[KeyboardEvent] = {
    Observable.create(DropOld(eventLimit)) { subscriber =>
      val c = SingleAssignmentCancelable()
      // Forced conversion, otherwise canceling will not work!
      val f: js.Function1[KeyboardEvent, Ack] = (e: KeyboardEvent) =>
        subscriber.onNext(e).syncOnStopOrFailure((_) => c.cancel())

      dom.document.addEventListener("keydown", f)
      dom.document.addEventListener("keyup", f)
      c := Cancelable(() => {
        dom.document.removeEventListener("keydown", f)
        dom.document.removeEventListener("keyup", f)
      })
    }
  }

  def wheelEventListener: Observable[WheelEvent] = {
    Observable.create(DropOld(eventLimit)) { subscriber =>
      val c = SingleAssignmentCancelable()
      // Forced conversion, otherwise canceling will not work!
      val f: js.Function1[WheelEvent, Ack] = (e: WheelEvent) =>
        subscriber.onNext(e).syncOnStopOrFailure((_) => c.cancel())

      dom.document.addEventListener("wheel", f)
      c := Cancelable(() => {
        dom.document.removeEventListener("wheel", f)
      })
    }
  }
}
