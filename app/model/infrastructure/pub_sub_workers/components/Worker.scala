package model.infrastructure.pub_sub_workers.components

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.pattern.pipe
import model.infrastructure.pub_sub_workers.components.Worker.{Execute, Response}
import model.infrastructure.pub_sub_workers.domain.WebsocketMessage._

import scala.concurrent.{ExecutionContextExecutor, Future}

object Worker {
  case class Execute(inMessage: InMessage)
  case class Response(outMessage: OutMessage)
  def props(task: InMessage => Future[OutMessage]): Props = Props(new Worker(task))
}
class Worker(task: InMessage => Future[OutMessage]) extends Actor with ActorLogging {

  implicit val dispatcher: ExecutionContextExecutor = context.system.dispatcher

  def receive: Receive = {
    case Execute(in: InMessage) =>
      log.info(s"Worked received ${in}")
      task(in).map(Response) pipeTo context.parent
  }
}
