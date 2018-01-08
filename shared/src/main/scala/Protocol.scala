package shared

object Protocol {
  sealed trait Message

  case class TimeMessage(sendTime: Long,
                  serverTime: Long,
                  receiveTime: Long) extends Message
}
