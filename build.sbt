import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*
import sbt.Keys.libraryDependencies

ThisBuild / organization := "org.beangle.commons"
ThisBuild / version := "6.0.4"
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

lazy val root = (project in file("."))
  .settings(
    name := "beangle-commons",
    common,
    libraryDependencies ++= Seq(slf4j % "optional", logback_classic % "optional", logback_core % "optional"),
    libraryDependencies ++= Seq(jexl3 % "optional", apache_commons_compress % "optional"),
    libraryDependencies ++= Seq(jul_to_slf4j % "optional", scalatest),
    libraryDependencies += "com.swoval" % "file-tree-views" % "2.1.12" % "optional"
  )
