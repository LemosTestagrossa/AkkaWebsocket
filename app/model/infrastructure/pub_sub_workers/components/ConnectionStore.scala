package model.infrastructure.pub_sub_workers.components

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import model.infrastructure.pub_sub_workers.components.DistributedPubSubInstance.DistributedPubSubInstanceRef
import model.infrastructure.pub_sub_workers.domain.ConnectionControl
import model.infrastructure.pub_sub_workers.domain.WebsocketMessage.{outTopic, OutMessage}

import scala.collection.mutable

object ConnectionStore {
  def props()(
      implicit
      pubsub: DistributedPubSubInstanceRef
  ): Props = Props(new ConnectionStore)
}
class ConnectionStore(
    implicit
    pubsub: DistributedPubSubInstanceRef
) extends Actor
    with ActorLogging {

  pubsub.subscribeToTopic(ConnectionControl.topic, self)
  pubsub.subscribeToTopic(outTopic, self)

  val actors: mutable.ListBuffer[ActorRef] = mutable.ListBuffer.empty

  def receive: Receive = {
    case OutMessage(out) =>
      log.info(s"ConnectionActor received ${out}. Broadcasting.")
      actors foreach (_ ! out)
    case ConnectionControl.AddOutboundConnection(outActor) =>
      log.info(s"ConnectionActor received AddOutboundConnection. Adding.")
      actors += outActor
    case ConnectionControl.RemoveOutboundConnection(outActor) =>
      log.info(s"ConnectionActor received RemoveOutboundConnection. Removing.")
      actors -= outActor
  }
}
