package com.example.ask
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream._
import akka.stream.scaladsl._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent._

object SimpleProducer extends App {
  implicit val actorSystem  = ActorSystem("SimpleProducer")
  implicit val materializer = ActorMaterializer()
  implicit val ec           = actorSystem.dispatcher

  val source: Source[Int, NotUsed] = Source(1 to 5)
  val outputTopic                  = "test-topic"
  val producerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  val consoleSink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)
  val producerSink                         = Producer.plainSink(producerSettings)

  // source.runWith(consoleSink)
  val result = source
    .map(_.toString)
    .map { n =>
      new ProducerRecord[String, String](outputTopic, n)
    }
    .runWith(producerSink)

  result.onComplete(_ => actorSystem.terminate())
}
