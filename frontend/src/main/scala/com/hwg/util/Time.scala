package com.hwg.util

import com.hwg.WebsocketClient
import shared.Protocol.TimeMessage
import monix.execution.Scheduler.Implicits.global

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.Date
import scala.scalajs.js

class Time(val client: WebsocketClient) {

  private case class TimeDelta(latency: Double, delta: Double)

  val samples: ArrayBuffer[TimeDelta] = ArrayBuffer()
  var currentDelta: Option[Long] = None
  val obsLimit = 10

  init()

  def init(): Unit = {
    client.getObservable.filter(_.isInstanceOf[TimeMessage]).foreach {
      case TimeMessage(sendTime, serverTime, receiveTime) =>
        val latency = receiveTime - sendTime
        val delta = receiveTime - serverTime + latency / 2

        val sample = TimeDelta(latency, delta)

        samples.append(sample)

        if (this.samples.length >= this.obsLimit) {
          updateDelta()
        } else {
          if (currentDelta.isEmpty) {
            this.setDelta(delta)
          }
        }
    }

    js.timers.setTimeout(nextReq) {
      timeRequestLoop()
    }
  }

  def updateDelta(): Unit = {
    while (this.samples.length >= this.obsLimit) {
      this.samples.remove(1)
    }

    val deltas = this.samples.map(_.delta)

    for (median <- Stats.median(deltas);
         stdDev <- Stats.stdDev(deltas)) yield {
      val filtered = deltas.filter(delta => delta < median + stdDev && delta > median - stdDev)

      Stats.mean(filtered).foreach(delta => setDelta(delta.toLong))
    }
  }

  private def setDelta(delta: Long): Unit = {
    currentDelta = Some(delta)
  }

  def now: Long = {
    new Date().getTime().toLong + currentDelta.getOrElse(0L)
  }


  private def nextReq: Long = {
    samples.length match {
      case 0 =>
        10
      case n if n < 10 =>
        (Math.random() * 1000 + 1000).toLong
      case _ =>
        (Math.random() * 5000 + 60000).toLong
    }
  }

  private def timeRequestLoop(): Unit = {
    if (client.alive) {
      client.send(TimeMessage(new Date().getTime().toLong, null, null))
    }

    js.timers.setTimeout(nextReq) {
      timeRequestLoop()
    }
  }
}
