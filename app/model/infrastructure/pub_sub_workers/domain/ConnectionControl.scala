package model.infrastructure.pub_sub_workers.domain

sealed trait ConnectionControl

object ConnectionControl {
  val topic = "ConnectionControl"
  case class RemoveOutboundConnection(actorRef: akka.actor.ActorRef) extends ConnectionControl
  case class AddOutboundConnection(actorRef: akka.actor.ActorRef) extends ConnectionControl
}
