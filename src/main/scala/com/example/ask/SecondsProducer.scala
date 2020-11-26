package com.example.ask
import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream._
import akka.stream.scaladsl._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.concurrent._
import scala.concurrent.duration._

object SecondsProducer extends App {
  implicit val actorSystem  = ActorSystem("SimpleProducer")
  implicit val materializer = ActorMaterializer()
  implicit val ec           = actorSystem.dispatcher
  // https://blog.colinbreck.com/patterns-for-streaming-measurement-data-with-akka-streams/

//  val source = Source.repeat(NotUsed).map(_ => builderFunction())
//  val source = Source.repeat("Elem")
  val source = Source.tick(1.second, 1.second, "tick")

  val outputTopic = "test-topic"
  val producerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  val consoleSink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)
  val producerSink                         = Producer.plainSink(producerSettings)

  val result = source
    .map { n =>
      val dateTime          = new DateTime()
      val dateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss")
      val dtString          = dateTimeFormatter.print(dateTime)
      println(s"Producing message: ${n}: $dtString")
      new ProducerRecord[String, String](outputTopic, dtString)
    }
//    .throttle(1, 1.second, 1, ThrottleMode.shaping)
    .runWith(producerSink)

  result.onComplete(_ => actorSystem.terminate())
}
