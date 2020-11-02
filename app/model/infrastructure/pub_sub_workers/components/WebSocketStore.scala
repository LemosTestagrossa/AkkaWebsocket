package model.infrastructure.pub_sub_workers.components

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import model.infrastructure.pub_sub_workers.PubSubWorkers
import model.infrastructure.pub_sub_workers.domain.ConnectionControl
import model.infrastructure.pub_sub_workers.domain.WebsocketMessage.{outTopic, OutMessage}
import play.api.mvc.WebSocket

import scala.collection.mutable

class WebSocketStore(factory: String => WebSocket) {

  private val webSockets = mutable.Map[String, WebSocket]()

  def socket(id: String): WebSocket =
    webSockets.get(id) match {
      case None =>
        webSockets.addOne(id, factory(id))
        webSockets(id)
      case Some(websocket) =>
        websocket
    }

}
