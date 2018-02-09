package io.findify.sqsmock.actions

import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import io.findify.sqsmock.messages.{CreateQueueResponse, ErrorResponse, GetQueueUrlResponse}
import io.findify.sqsmock.model.{Queue, QueueCache}

import scala.collection.mutable

/**
  * Created by shutty on 3/29/16.
  */
class GetQueueUrlWorker(account:Long, port:Int, queues:mutable.Map[String,QueueCache], system:ActorSystem) extends Worker {
  val log = Logger(this.getClass, "create_queue_worker")


  val attributeFormat = """Attribute\.([0-9])\.([A-Za-z]+)""".r
  def process(fields:Map[String,String]) = fields.get("QueueName") match {
    case Some(queueName) =>
      val attrs = attributes(fields)
      val q = Queue(
        account,
        queueName,
        port,
        visibilityTimeout = fields.getOrElse("VisibilityTimeout", "30").toInt
      )
      val result = for (
        queue <- queues.get(q.url)
      ) yield {
        log.debug(s"Get queue $q, url=${q.url}")
        HttpResponse(StatusCodes.OK, entity = GetQueueUrlResponse(q).toXML.toString())
      }
      result.getOrElse{
        log.warn("cannot get url: possibly, some request parameter is missing")
        HttpResponse(StatusCodes.BadRequest, entity = ErrorResponse("Sender", "InvalidParameterValue", "oops").toXML.toString())
      }
    case None =>
      log.warn("missing QueueName parameter, cannot create queue")
      HttpResponse(StatusCodes.BadRequest, entity = ErrorResponse("Sender", "InvalidParameterValue", "no QueueName parameter").toXML.toString())
  }

  private def attributes(fields:Map[String, String]) = {
    def nameValue(list:Iterable[(Int,String,String)]) = for (
      name <- list.find(_._2 == "Name").map(_._3);
      value <- list.find(_._2 == "Value").map(_._3)
    ) yield name -> value

    fields.flatMap { case (key, value) => value match {
      case attributeFormat(id, name) => Some((id.toInt, name, value))
      case _ => None
    }}.groupBy(_._1)
      .flatMap { case (index, values) => nameValue(values)}
  }
}
