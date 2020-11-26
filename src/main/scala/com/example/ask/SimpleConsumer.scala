package com.example.ask

import java.util.UUID

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer

import scala.util.{Failure, Success}

object SimpleConsumer extends App {
  implicit val system       = ActorSystem("SimpleProducer")
  implicit val materializer = ActorMaterializer()
  implicit val ec           = system.dispatcher

  val consumerSettings =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withGroupId(UUID.randomUUID().toString)

  val topic = "test-topic"

  def metadataFromRecord(record: ConsumerRecord[String, String]): String =
    record.timestamp().toString

  def business(str: String, str1: String) = {
    println(str)
    println(str1)
    true
  }

  val committerSettings = CommitterSettings(system)
  val done = Consumer
    .plainSource(consumerSettings, Subscriptions.topics(topic))
    .runWith(Sink.foreach(println))

  done.onComplete {
    case Success(_)   => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }
//  val consoleSink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)
//  consumerSource.runWith(consoleSink)
}
