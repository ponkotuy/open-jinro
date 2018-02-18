name := """open-jinro"""
organization := "com.ponkotuy"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

val CirceVersion = "0.9.1"
val SkinnyVersion = "2.5.2"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.skinny-framework" %% "skinny-orm" % SkinnyVersion,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "com.dripower" %% "play-circe" % "2609.0",
  "org.springframework.security" % "spring-security-core" % "5.0.1.RELEASE"
) ++ circes

val circes = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-java8"
).map(_ % CirceVersion)
