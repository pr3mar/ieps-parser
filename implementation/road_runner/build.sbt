name := "road_runner"

version := "0.1"

scalaVersion := "2.12.8"

lazy val root = (project in file("."))
  .settings(
    name := "IEPS-crawler",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "joda-time" % "joda-time" % "2.7",
      "org.jsoup" % "jsoup" % "1.11.3"
    )
  )