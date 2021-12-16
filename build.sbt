import org.beangle.parent.Dependencies._
import org.beangle.parent.Settings._

ThisBuild / organization := "org.beangle.commons"
ThisBuild / version := "5.2.13-SNAPSHOT"
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

val commonDeps = Seq(slf4j, logback_classic, logback_core, scalatest)

lazy val root = (project in file("."))
  .settings()
  .aggregate(core,text,csv,dbf,commons_file)

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-commons-core",
    common,
    libraryDependencies ++= commonDeps
  )

lazy val text = (project in file("text"))
  .settings(
    name := "beangle-commons-text",
    common,
    libraryDependencies ++= commonDeps
  ).dependsOn(core)

lazy val csv = (project in file("csv"))
  .settings(
    name := "beangle-commons-csv",
    common,
    libraryDependencies ++= commonDeps
  ).dependsOn(core)

lazy val dbf = (project in file("dbf"))
  .settings(
    name := "beangle-commons-dbf",
    common,
    libraryDependencies ++= commonDeps
  ).dependsOn(core)

lazy val commons_file = (project in file("file"))
  .settings(
    name := "beangle-commons-file",
    common,
    libraryDependencies ++= (commonDeps ++ Seq(apache_commons_compress))
  ).dependsOn(core)

publish / skip := true
