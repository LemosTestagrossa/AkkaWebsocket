package model.infrastructure.pub_sub_workers.components

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import model.infrastructure.pub_sub_workers.components.DistributedPubSubInstance.DistributedPubSubInstanceRef
import model.infrastructure.pub_sub_workers.domain.WebsocketMessage._
import model.infrastructure.pub_sub_workers.domain.{ConnectionControl, _}

object ConnectionLifecycleListener {
  def props(parseStep: Serialized => Either[String, Seq[InMessage]])(out: ActorRef)(
      implicit
      pubsub: DistributedPubSubInstanceRef
  ) =
    Props(new ConnectionLifecycleListener(parseStep, out))

}

class ConnectionLifecycleListener(
    parseStep: Serialized => Either[String, Seq[InMessage]],
    out: ActorRef
)(
    implicit
    pubsub: DistributedPubSubInstanceRef
) extends Actor
    with ActorLogging {

  override def aroundPreStart(): Unit = {
    log.info("ConnectionLifecycleListenerActor informed of its creation")
    pubsub.publishToTopic(ConnectionControl.topic, ConnectionControl.AddOutboundConnection(out))
  }
  override def aroundPostStop(): Unit = {
    log.info("ConnectionLifecycleListenerActor informed of its removal")
    pubsub.publishToTopic(ConnectionControl.topic, ConnectionControl.RemoveOutboundConnection(out))
  }

  def receive: Receive = {
    case in: Serialized =>
      parseStep(in) match {
        case Left(value) => out ! s"[Message Rejection] Reason: $value"
        case Right(value) =>
          value.toIndexedSeq.foreach { task: InMessage =>
            log.info(s"ConnectionLifecycleActor received task $task. Publishing.")
            pubsub.publishToTopicWithConsumerGroup(inTopic, task)
          }
      }
  }
}
