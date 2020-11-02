package controllers

import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.Supervision
import model.infrastructure.pub_sub_workers.PubSubWorkers
import model.infrastructure.pub_sub_workers.components.WebSocketStore
import model.infrastructure.pub_sub_workers.domain.WebsocketMessage.{InMessage, OutMessage}

import scala.collection.mutable
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
import play.api.mvc._
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.Materializer

class Application @Inject() (cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer)
    extends AbstractController(cc) {

  object MockServices {
    object Database {
      val mockDatabase = Map(
        "already processed" -> Set("1", "2", "3")
      )
    }
    object Validations {
      def idempotencyValidation(revisionId: String): Boolean =
        Database.mockDatabase("already processed").contains(revisionId)
    }
  }

  val parseStep: String => Either[String, Seq[InMessage]] = {
    case s"""{"revisionId":$revisionId,"tasks":[$tasks]}""" =>
      MockServices.Validations.idempotencyValidation(revisionId) match {
        case true =>
          Left(s"We already processed a message with a revisionId of ${revisionId}")
        case false =>
          Right(tasks.split(",").map(InMessage))
      }
    case _ =>
      Left("Your message should look like this: " + s"""{"revisionId":1,"tasks":[...]}""")
  }

  val algorithm: InMessage => Future[OutMessage] =
    in =>
      Future {
        Thread.sleep(1000)
        OutMessage(s"Done $in")
      }(system.dispatcher)

  val webSocketStore = new WebSocketStore(
    factory = (instanceId: String) => new PubSubWorkers(instanceId, parseStep, algorithm, 10).socket
  )

  def socket(companyId: String = "CocaCola", documentId: String = "ReportOfApril2020"): WebSocket =
    webSocketStore.socket(s"company/$companyId/document/$documentId")
}
