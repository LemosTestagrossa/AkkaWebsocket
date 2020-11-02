package model.infrastructure.pub_sub_workers.components

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, SubscribeAck}
import model.infrastructure.pub_sub_workers.components.DistributedPubSubInstance.{
  PublishToTopic,
  PublishToTopicWithConsumerGroup,
  SubscribeToTopic,
  SubscribeToTopicWithConsumerGroup
}

object DistributedPubSubInstance {
  case class PublishToTopic(topic: String, message: Any)
  case class PublishToTopicWithConsumerGroup(topic: String, message: Any)
  case class SubscribeToTopic(topic: String, subscriber: ActorRef)
  case class SubscribeToTopicWithConsumerGroup(topic: String, group: String, subscriber: ActorRef)

  case class DistributedPubSubInstanceRef(private val actorRef: ActorRef) {
    def subscribeToTopic(topic: String, subscriber: ActorRef): Unit =
      actorRef ! SubscribeToTopic(topic, subscriber)
    def subscribeToTopicWithConsumerGroup(topic: String, group: String, subscriber: ActorRef): Unit =
      actorRef ! SubscribeToTopicWithConsumerGroup(topic, group, subscriber)
    def publishToTopic(topic: String, message: Any): Unit =
      actorRef ! PublishToTopic(topic, message)
    def publishToTopicWithConsumerGroup(topic: String, message: Any): Unit =
      actorRef ! PublishToTopicWithConsumerGroup(topic, message)
  }
  def props(instanceId: String): Props = Props(new DistributedPubSubInstance(instanceId))

}
class DistributedPubSubInstance(instanceId: String) extends Actor with ActorLogging {
  val mediator = DistributedPubSub(context.system).mediator

  def segregateTopic(topic: String): String = s"${topic}-$instanceId"

  override def receive: Receive = {
    case SubscribeToTopic(topic, subscriber) =>
      mediator ! Subscribe(
        topic = this segregateTopic topic,
        group = None,
        subscriber
      )
    case SubscribeToTopicWithConsumerGroup(topic, group, subscriber) =>
      mediator ! Subscribe(
        topic = this segregateTopic topic,
        group = Some(group),
        subscriber
      )
    case PublishToTopic(topic, message) =>
      mediator ! Publish(
        topic = this segregateTopic topic,
        message
      )
    case PublishToTopicWithConsumerGroup(topic, message) =>
      mediator ! Publish(
        topic = this segregateTopic topic,
        message,
        sendOneMessageToEachGroup = true
      )
    case SubscribeAck(subscribed: Subscribe) =>
      log.info(s"DistributedPubSubInstance $instanceId subscribed ${subscribed}")
  }
}
