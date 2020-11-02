package model.infrastructure.pub_sub_workers.domain

sealed trait WebsocketMessage

object WebsocketMessage {
  val outTopic = "out"
  val inTopic = "in"
  case class InMessage(message: Serialized) extends WebsocketMessage
  case class OutMessage(message: Serialized) extends WebsocketMessage
}
