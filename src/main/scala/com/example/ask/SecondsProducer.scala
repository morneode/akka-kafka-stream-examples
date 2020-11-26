package com.example.ask
import java.nio.file.Paths
import java.util.{Calendar, UUID}

import scala.concurrent._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ProducerMessage
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object SecondsProducer extends App {
  implicit val actorSystem  = ActorSystem("SimpleProducer")
  implicit val materializer = ActorMaterializer()
  implicit val ec           = actorSystem.dispatcher
  // https://blog.colinbreck.com/patterns-for-streaming-measurement-data-with-akka-streams/

  def builderFunction() = {
//    System.currentTimeMillis()
//    Thread.sleep(1000)
//    val now = Calendar.getInstance()
//    now.toString
//    val dateTime          = new DateTime()
//    val dateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss")
//    dateTimeFormatter.print(dateTime)
    "builder"
  }
  val source = Source.repeat(NotUsed).map(_ => builderFunction())

  val outputTopic = "test-topic"
  val producerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  val consoleSink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)
  val producerSink                         = Producer.plainSink(producerSettings)

  // source.runWith(consoleSink)
  val result = source
    .map { n =>
      println(s"Producing message: ${n}")
      val dateTime          = new DateTime()
      val dateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss")
      val dtString          = dateTimeFormatter.print(dateTime)
      new ProducerRecord[String, String](outputTopic, dtString)
    }
    .throttle(1, 1.second, 1, ThrottleMode.shaping)
    .runWith(producerSink)

  result.onComplete(_ => actorSystem.terminate())
}
