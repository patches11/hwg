package com.hwg

/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
import akka.actor._
import akka.stream.{ Materializer, OverflowStrategy }
import akka.stream.scaladsl.{ Sink, Keep, Source, Flow }

/**
  * Provides a flow that is handled by an actor.
  *
  * See https://github.com/akka/akka/issues/16985.
  */
object ActorFlow {

  /**
    * Create a flow that is handled by an actor.
    *
    * Messages can be sent downstream by sending them to the actor passed into the props function.  This actor meets
    * the contract of the actor returned by [[http://doc.akka.io/api/akka/current/index.html#akka.stream.scaladsl.Source$@actorRef[T](bufferSize:Int,overflowStrategy:akka.stream.OverflowStrategy):akka.stream.scaladsl.Source[T,akka.actor.ActorRef] akka.stream.scaladsl.Source.actorRef]].
    *
    * The props function should return the props for an actor to handle the flow. This actor will be created using the
    * passed in [[http://doc.akka.io/api/akka/current/index.html#akka.actor.ActorRefFactory akka.actor.ActorRefFactory]]. Each message received will be sent to the actor - there is no back pressure,
    * if the actor is unable to process the messages, they will queue up in the actors mailbox. The upstream can be
    * cancelled by the actor terminating itself.
    *
    * @param props A function that creates the props for actor to handle the flow.
    * @param bufferSize The maximum number of elements to buffer.
    * @param overflowStrategy The strategy for how to handle a buffer overflow.
    */
  def actorRef[In, Out](props: ActorRef => Props, bufferSize: Int = 16, overflowStrategy: OverflowStrategy = OverflowStrategy.dropNew)(implicit factory: ActorRefFactory, mat: Materializer): Flow[In, Out, _] = {

    val (outActor, publisher) = Source.actorRef[Out](bufferSize, overflowStrategy)
      .toMat(Sink.asPublisher(false))(Keep.both).run()

    Flow.fromSinkAndSource(
      Sink.actorRef(factory.actorOf(Props(new Actor {
        val flowActor = context.watch(context.actorOf(props(outActor), "flowActor"))

        def receive = {
          case Status.Success(_) | Status.Failure(_) => flowActor ! PoisonPill
          case Terminated(_) => context.stop(self)
          case other => flowActor ! other
        }

        override def supervisorStrategy = OneForOneStrategy() {
          case _ => SupervisorStrategy.Stop
        }
      })), Status.Success(())),
      Source.fromPublisher(publisher)
    )
  }

  case class Init(actorRef: ActorRef)

  private class Blank extends Actor {
    override def receive: Receive = PartialFunction.empty
  }

  def actorRef2[In, Out](props: ActorRef => Props, bufferSize: Int = 16, overflowStrategy: OverflowStrategy = OverflowStrategy.dropNew)(implicit factory: ActorRefFactory, mat: Materializer): Flow[In, Out, _] = {
    val blank = factory.actorOf(Props(new Blank))
    val shipActor = factory.actorOf(props(blank))
    val out = Source.actorRef[Out](bufferSize, OverflowStrategy.dropHead).mapMaterializedValue(ref => shipActor ! Init(ref))

    val sink = Sink.actorRef[In](shipActor, Status.Success(()))

    Flow.fromSinkAndSource(sink, out)
  }

}
