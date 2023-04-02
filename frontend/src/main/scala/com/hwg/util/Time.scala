package com.hwg.util

import com.hwg.WebsocketClient
import com.hwg.Protocol.TimeMessage


import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.Date
import scala.scalajs.js
import slogging.LazyLogging

class Time(val client: WebsocketClient) extends LazyLogging {
  import monix.execution.Scheduler.Implicits.global

  private case class TimeDelta(latency: Double, delta: Double)

  private val samples: ArrayBuffer[TimeDelta] = ArrayBuffer()
  var currentDelta: Option[Long] = None
  val obsLimit = 10

  init()

  def init(): Unit = {
    client.getObservable.collect {
      case TimeMessage(sendTime, serverTime, receiveTime) =>
        val latency = receiveTime - sendTime
        val delta = receiveTime - serverTime + latency / 2
        logger.info(f"time delta ${delta} latency ${latency}")

        val sample = TimeDelta(latency, delta)

        samples.append(sample)

        if (this.samples.length >= this.obsLimit) {
          updateDelta()
        } else {
          if (currentDelta.isEmpty) {
            this.setDelta(delta)
          }
        }
    }.subscribe()

    js.timers.setTimeout(nextReq) {
      timeRequestLoop()
    }
  }

  def updateDelta(): Unit = {
    if (samples.length > obsLimit) {
      this.samples.remove(samples.length - obsLimit)
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

  def nowRaw: Long = new Date().getTime().toLong

  def now: Long = {
    //new Date().getTime().toLong + currentDelta.getOrElse(0L)
    nowRaw
  }


  private def nextReq: Long = {
    samples.length match {
      case 0 =>
        100
      case n if n < obsLimit =>
        (Math.random() * 1000 + 1000).toLong
      case _ =>
        (Math.random() * 5000 + 10000).toLong
    }
  }

  private def timeRequestLoop(): Unit = {
    if (client.alive) {
      client.send(TimeMessage(new Date().getTime().toLong, 0, 0))
    }

    js.timers.setTimeout(nextReq) {
      timeRequestLoop()
    }
  }
}
