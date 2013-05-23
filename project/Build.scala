import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.beangle"
  val buildVersion = "4.0.0-SNAPSHOT"
  val buildScalaVersion = "2.10.0"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt := ShellPrompt.buildShellPrompt
  )
}

object ShellPrompt {

  object devnull extends ProcessLogger {
    def info(s: => String) {}

    def error(s: => String) {}

    def buffer[T](f: => T): T = f
  }

  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
    )

  val buildShellPrompt = {
    (state: State) => {
      val currProject = Project.extract(state).currentProject.id
      "%s:%s:%s> ".format(
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}

object Dependencies {
  val h2Ver = "1.3.148"
  val slf4jVer = "1.6.1"
  val junitVer = "4.10"
  val testngVer = "6.5.2"

  val springVer="3.1.1.RELEASE"

  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVer
  val asm ="asm" % "asm" % "3.3"
  val servletapi= "javax.servlet" % "servlet-api" % "2.4"

  val junit = "junit" % "junit" % junitVer % "test"
  val testng= "org.testng" % "testng" % testngVer % "test"
  val logbackClassic="ch.qos.logback" % "logback-classic" % "1.0.3"
  val logbackCore="ch.qos.logback" % "logback-core" % "1.0.3"

  val dbcp = "commons-dbcp" % "commons-dbcp" % "1.3"
  val springJdbc="org.springframework" % "spring-jdbc" % springVer
  val commonCollection="commons-collections" % "commons-collections" % "3.2.1"
  val javadbf="com.linuxense" % "javadbf" %"0.4.0"
  val poi = "org.apache.poi" % "poi" % "3.7"
  val jxls ="net.sf.jxls" % "jxls-core" % "1.0.1"

  val jpa = "org.hibernate.javax.persistence" % "hibernate-jpa-2.0-api" % "1.0.1.Final"
  val validation ="javax.validation" % "validation-api" % "1.0.0.GA"
}

object Resolvers {
  val m2repo  = "Local Maven2 Repo" at "file://" + Path.userHome + "/.m2/repository"
}

object BeangleBuild extends Build {

  import Dependencies._
  import BuildSettings._
  import Resolvers._
 
  val commonDeps = Seq(slf4j, logbackClassic,logbackCore, junit,testng)

  lazy val commons = Project("beangle-commons", file("."), settings = buildSettings) aggregate (commons_core,commons_web,commons_jpa,commons_jdbc)

  lazy val commons_core = Project(
    "beangle-commons-core",
    file("org.beangle.commons.core"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps ++ Seq(asm))  ++ Seq(resolvers +=m2repo)
  )

  lazy val commons_web = Project(
    "beangle-commons-web",
    file("org.beangle.commons.web"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps ++ Seq(servletapi,validation)) 
               ++ Seq(resolvers += m2repo)
  ) dependsOn(commons_core)

  lazy val commons_jpa = Project(
    "beangle-commons-jpa",
    file("org.beangle.commons.jpa"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps ++ Seq(validation,jpa,poi,jxls)) 
               ++ Seq(resolvers += m2repo)
  ) dependsOn(commons_core)

  lazy val commons_jdbc = Project(
    "beangle-commons-jdbc",
    file("org.beangle.commons.jdbc"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps ++ Seq(dbcp))  
               ++ Seq(resolvers +=m2repo)
  ) dependsOn(commons_core)
}
