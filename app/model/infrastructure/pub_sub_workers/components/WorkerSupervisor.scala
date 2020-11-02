package model.infrastructure.pub_sub_workers.components

import java.util.UUID

import akka.actor._
import akka.pattern._
import model.infrastructure.pub_sub_workers.components.DistributedPubSubInstance.DistributedPubSubInstanceRef
import model.infrastructure.pub_sub_workers.domain.WebsocketMessage._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}

object WorkerSupervisor {
  def props(
      task: InMessage => Future[OutMessage],
      supervisionStrategy: SupervisorStrategy.Decider
  )(
      implicit
      pubsub: DistributedPubSubInstanceRef
  ): Props =
    Props(new WorkerSupervisor(task, supervisionStrategy))
}
class WorkerSupervisor(
    task: InMessage => Future[OutMessage],
    supervisionStrategy: SupervisorStrategy.Decider
)(
    implicit
    pubsub: DistributedPubSubInstanceRef
) extends Actor
    with ActorLogging {

  pubsub.subscribeToTopicWithConsumerGroup(inTopic, "Gremio Sindical", self)

  implicit val dispatcher: ExecutionContextExecutor = context.system.dispatcher

  def workerProps: Props = Worker.props(task)
  def supervisedWorker(workerProps: Props): ActorRef =
    context.actorOf(
      BackoffSupervisor.props(
        BackoffOpts
          .onFailure(
            workerProps,
            childName = s"SupervisedWorker-${UUID.randomUUID()}",
            minBackoff = 3.seconds,
            maxBackoff = 30.seconds,
            randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
          )
          .withManualReset
          .withSupervisorStrategy(OneForOneStrategy()(supervisionStrategy))
      )
    )

  def receive: Receive = {
    case in: InMessage =>
      log.info(s"Worked received ${in}")
      supervisedWorker(workerProps) ! Worker.Execute(in)
    case Worker.Response(done: OutMessage) =>
      pubsub.publishToTopic(outTopic, done)
  }
}
