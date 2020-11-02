package model.infrastructure.pub_sub_workers

import akka.actor.{ActorRef, ActorSystem, SupervisorStrategy}
import akka.serialization.jackson.Compression.Algoritm
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.{Materializer, Supervision}
import javax.inject.Inject
import model.infrastructure.pub_sub_workers.components.DistributedPubSubInstance.DistributedPubSubInstanceRef
import model.infrastructure.pub_sub_workers.components.{
  ConnectionLifecycleListener,
  ConnectionStore,
  DistributedPubSubInstance,
  Worker,
  WorkerSupervisor
}
import model.infrastructure.pub_sub_workers.domain.WebsocketMessage.{InMessage, OutMessage}
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket

import scala.concurrent.Future

class PubSubWorkers(
    instanceId: String,
    parseStep: String => Either[String, Seq[InMessage]],
    algorithm: InMessage => Future[OutMessage],
    numberOfWorkers: Int = 10,
    supervisionStrategy: SupervisorStrategy.Decider = { case _ => SupervisorStrategy.Restart }
)(implicit system: ActorSystem) {

  implicit val pubsub: DistributedPubSubInstanceRef =
    DistributedPubSubInstanceRef(
      system.actorOf(DistributedPubSubInstance.props(instanceId))
    )

  val workersPool: Seq[ActorRef] =
    (1 to numberOfWorkers)
      .map { _ =>
        WorkerSupervisor.props(algorithm, supervisionStrategy)
      }
      .map(system.actorOf)

  val connectionsStore: ActorRef = system.actorOf(ConnectionStore.props())

  def socket: WebSocket = WebSocket.accept[String, String] { _ =>
    ActorFlow.actorRef(ConnectionLifecycleListener.props(parseStep), bufferSize = 10000)
  }
}
