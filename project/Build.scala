import sbt.Keys._
import sbt._

object BuildSettings {
  val buildOrganization = "org.beangle.commons"
  val buildVersion = "5.2.5-SNAPSHOT"
  val buildScalaVersion = "3.0.1"

  val commonSettings = Seq(
    organization := buildOrganization,
    organizationName  := "The Beangle Software",
    licenses += ("GNU Lesser General Public License version 3", new URL("http://www.gnu.org/licenses/lgpl-3.0.txt")),
    startYear := Some(2005),
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions := Seq("-encoding", "UTF-8", "-feature", "-unchecked", "-deprecation"
      , "-language:implicitConversions", "-Xtarget:11", "-Xfatal-warnings"),
    crossPaths := true,

    publishMavenStyle := true,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishM2Configuration := publishM2Configuration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),

    versionScheme := Some("early-semver"),
    pomIncludeRepository := { _ => false },// Remove all additional repository other than Maven Central from POM
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  )
}

object Dependencies {
  val slf4jVer = "1.7.32"
  val logbackVer = "1.2.4"
  val scalatestVer = "3.2.9"
  val servletapiVer ="5.0.0"
  val compressVer = "1.21"

  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVer
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVer % "test"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVer % "test"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVer % "test"

  val commonsCompress = "org.apache.commons" % "commons-compress"  % compressVer
  val commonDeps = Seq(slf4j, logbackClassic, logbackCore, scalatest)

}

