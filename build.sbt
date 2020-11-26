name := "mirror"
organization := "com.example.akka-stream-kafka"
scalaVersion := "2.12.12"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.5"
libraryDependencies += "ch.qos.logback"     % "logback-classic"   % "1.2.2" % Runtime
libraryDependencies += "joda-time"          % "joda-time"         % "2.10.8"

// enablePlugins(JavaAppPackaging)
// enablePlugins(DockerPlugin)
// dockerBaseImage := "java:openjdk-8-jre"
// dockerRepository := Some("docker-hub-username")
