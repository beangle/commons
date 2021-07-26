import sbt.Keys._
import sbt._

object BuildSettings {
  val buildOrganization = "org.beangle.commons"
  val buildVersion = "5.2.4"
  val buildScalaVersion = "3.0.1"

  val commonSettings = Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions := Seq("-encoding", "UTF-8", "-feature", "-unchecked", "-deprecation"
      , "-language:implicitConversions", "-Xtarget:11", "-Xfatal-warnings"),
    crossPaths := true)
}

object Dependencies {
  val slf4jVer = "1.7.32"
  val mockitoVer = "3.11.1"
  val logbackVer = "1.2.4"
  val scalatestVer = "3.2.9"
  val scalaxmlVer="2.0.1"
  val servletapiVer ="5.0.0"
  val compressVer = "1.20"

  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVer
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVer % "test"
  val scalaxml = "org.scala-lang.modules" %% "scala-xml" % scalaxmlVer
  val mockito = "org.mockito" % "mockito-core" % mockitoVer % "test"

  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVer % "test"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVer % "test"

  val commonsCompress = "org.apache.commons" % "commons-compress"  % compressVer
  val commonDeps = Seq(slf4j, logbackClassic, logbackCore, scalatest)

}

