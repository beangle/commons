import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.beangle.commons"
  val buildVersion = "4.0.0-SNAPSHOT"
  val buildScalaVersion = "2.10.2"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt := ShellPrompt.buildShellPrompt,
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
  val h2Ver = "1.3.172"
  val slf4jVer = "1.6.6"
  val mockitoVer = "1.9.5"
  val logbackVer = "1.0.7"
  val scalatestVer = "2.0.M5b"

  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVer
  val asm = "asm" % "asm" % "3.3"
  val scalatest = "org.scalatest" % "scalatest_2.10" % scalatestVer % "test"
  val mockito = "org.mockito" % "mockito-core" % mockitoVer % "test"

  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVer % "test"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVer % "test"

  val h2 = "com.h2database" % "h2" % h2Ver
  val dbcp = "commons-dbcp" % "commons-dbcp" % "1.3"
  val jpa = "org.hibernate.javax.persistence" % "hibernate-jpa-2.0-api" % "1.0.1.Final"
  val validation = "javax.validation" % "validation-api" % "1.0.0.GA"

  val servletapi = "javax.servlet" % "servlet-api" % "2.4"
  val javamail = "javax.mail" % "mail" % "1.4"
  val greenmail = "com.icegreen" % "greenmail" % "1.3.1b"
}

object Resolvers {
  val m2repo = "Local Maven2 Repo" at "file://" + Path.userHome + "/.m2/repository"
}

object BeangleBuild extends Build {

  import Dependencies._
  import BuildSettings._
  import Resolvers._

  val commonDeps = Seq(slf4j, logbackClassic, logbackCore, scalatest)

  lazy val commons = Project("beangle-commons", file("."), settings = buildSettings) aggregate (commons_core, commons_web, commons_jpa, commons_jdbc, commons_message)

  lazy val commons_core = Project(
    "beangle-commons-core",
    file("core"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps ++ Seq(asm)) ++ Seq(resolvers += m2repo))

  lazy val commons_web = Project(
    "beangle-commons-web",
    file("web"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps ++ Seq(servletapi, validation, mockito))
      ++ Seq(resolvers += m2repo)) dependsOn (commons_core)

  lazy val commons_jpa = Project(
    "beangle-commons-jpa",
    file("jpa"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps ++ Seq(validation, jpa))
      ++ Seq(resolvers += m2repo)) dependsOn (commons_core)

  lazy val commons_jdbc = Project(
    "beangle-commons-jdbc",
    file("jdbc"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps ++ Seq(h2, dbcp))
      ++ Seq(resolvers += m2repo)) dependsOn (commons_core)

  lazy val commons_message = Project(
    "beangle-commons-message",
    file("message"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps ++ Seq(javamail, greenmail))
      ++ Seq(resolvers += m2repo)) dependsOn (commons_core)

}
