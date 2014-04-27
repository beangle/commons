import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.beangle.commons"
  val buildVersion = "4.1.0-SNAPSHOT"
  val buildScalaVersion = "2.11.0"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt := ShellPrompt.buildShellPrompt,
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-target:jvm-1.6","-optimise","-Yinline-warnings"),
    crossPaths := false)
}

object ShellPrompt {

  object devnull extends ProcessLogger {
    def info(s: ⇒ String) {}

    def error(s: ⇒ String) {}

    def buffer[T](f: ⇒ T): T = f
  }

  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
    getOrElse "-" stripPrefix "## ")

  val buildShellPrompt = {
    (state: State) ⇒
      {
        val currProject = Project.extract(state).currentProject.id
        "%s:%s:%s> ".format(
          currProject, currBranch, BuildSettings.buildVersion)
      }
  }
}

object Dependencies {
  val slf4jVer = "1.7.7"
  val mockitoVer = "1.9.5"
  val logbackVer = "1.1.2"
  val scalatestVer = "2.1.3"

  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVer
  val scalatest = "org.scalatest" % "scalatest_2.11" % scalatestVer % "test"
  val mockito = "org.mockito" % "mockito-core" % mockitoVer % "test"
  val junit = "junit" % "junit" % "4.11" % "test"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVer % "test"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVer % "test"

  val validation = "javax.validation" % "validation-api" % "1.0.0.GA"
  val servletapi = "javax.servlet" % "javax.servlet-api" % "3.1.0"
}

object Resolvers {
  val m2repo = "Local Maven2 Repo" at "file://" + Path.userHome + "/.m2/repository"
}

object BeangleBuild extends Build {

  import Dependencies._
  import BuildSettings._
  import Resolvers._

  val commonDeps = Seq(slf4j, logbackClassic, logbackCore, scalatest, junit)

  lazy val commons = Project("beangle-commons", file("."), settings = buildSettings) aggregate (commons_core, commons_web)

  lazy val commons_core = Project(
    "beangle-commons-core",
    file("core"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps) ++ Seq(resolvers += m2repo))

  lazy val commons_web = Project(
    "beangle-commons-web",
    file("web"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps ++ Seq(servletapi, validation, mockito))
      ++ Seq(resolvers += m2repo)) dependsOn (commons_core)
}
