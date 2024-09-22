import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*
import sbt.Keys.libraryDependencies

ThisBuild / organization := "org.beangle.commons"
ThisBuild / version := "5.6.18"
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/beangle/commons"),
    "scm:git@github.com:beangle/commons.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The Beangle Commons Library"
ThisBuild / homepage := Some(url("http://beangle.github.io/commons/index.html"))

val commonDeps = Seq(slf4j, logback_classic % "test", logback_core % "test", scalatest)

lazy val root = (project in file("."))
  .settings(
    name := "beangle-commons",
    common,
    libraryDependencies ++= commonDeps,
    libraryDependencies += jexl3 % "test",
    libraryDependencies += jcl_over_slf4j % "test",
    libraryDependencies += scalaxml % "optional",
    libraryDependencies += apache_commons_compress % "optional"
  )
