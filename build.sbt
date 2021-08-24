import org.beangle.bom.sbt.BeangleBom._

ThisBuild / organization := "org.beangle.commons"
ThisBuild / version := "5.2.6-SNAPSHOT"
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/beangle/commons"),
    "scm:git@github.com:beangle/commons.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "chaostone",
    name  = "Tihua Duan",
    email = "duantihua@gmail.com",
    url   = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The Beangle Commons Library"
ThisBuild / homepage := Some(url("http://beangle.github.io/commons/index.html"))

val commonDeps = Seq(Slf4j, LogbackClassic, LogbackCore, Scalatest)

lazy val root = (project in file("."))
  .settings()
  .aggregate(core,text,csv,dbf,commons_file)

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-commons-core",
    CommonSettings,
    libraryDependencies ++= commonDeps
  )

lazy val text = (project in file("text"))
  .settings(
    name := "beangle-commons-text",
    CommonSettings,
    libraryDependencies ++= commonDeps
  ).dependsOn(core)

lazy val csv = (project in file("csv"))
  .settings(
    name := "beangle-commons-csv",
    CommonSettings,
    libraryDependencies ++= commonDeps
  ).dependsOn(core)

lazy val dbf = (project in file("dbf"))
  .settings(
    name := "beangle-commons-dbf",
    CommonSettings,
    libraryDependencies ++= commonDeps
  ).dependsOn(core)

lazy val commons_file = (project in file("file"))
  .settings(
    name := "beangle-commons-file",
    CommonSettings,
    libraryDependencies ++= (commonDeps ++ Seq(ApacheCommonsCompress))
  ).dependsOn(core)

publish / skip := true
