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
  var currentLatency: Option[Long] = None
  val obsLimit = 10

  init()

  def init(): Unit = {
    client.getObservable.collect {
      case TimeMessage(sendTime, serverTime, receiveTime) =>
        val latency = receiveTime - sendTime
        val delta = receiveTime - serverTime + latency / 2

        val sample = TimeDelta(latency, delta)

        samples.append(sample)

        updateMetrics()

    }.subscribe()

    js.timers.setTimeout(nextReq) {
      timeRequestLoop()
    }
  }

  private def updateMetrics(): Unit = {
    if (samples.length > obsLimit) {
      this.samples.remove(samples.length - obsLimit)
    }

    val deltas = this.samples.map(_.delta)
    val latencies = this.samples.map(_.latency)

    for (median <- Stats.median(deltas);
         stdDev <- Stats.stdDev(deltas)) yield {
      val filtered = deltas.filter(delta => delta < median + stdDev && delta > median - stdDev)

      Stats.mean(filtered).foreach(delta => {
        this.currentDelta = Some(delta.toLong)
      })

    }
    for (median <- Stats.median(latencies);
         stdDev <- Stats.stdDev(latencies)) yield {
      val filtered = latencies.filter(latency => latency < median + stdDev && latency > median - stdDev)

      Stats.mean(filtered).foreach(latency => {
        this.currentLatency = Some(latency.toLong)
      })

    }

  }


  def nowRaw: Long = new Date().getTime().toLong

  def now: Long = {
    new Date().getTime().toLong + currentDelta.getOrElse(0L)
  }

  def estimatedSendTime: Long = {
    nowRaw - getLatency / 2
  }

  def getLatency: Long = {
    this.currentLatency.getOrElse(0)
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
