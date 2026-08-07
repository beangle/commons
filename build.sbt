import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

organization := "org.beangle.commons"
version := "6.2.2"
scmInfo := Some(
  ScmInfo(
    uri("https://github.com/beangle/commons"),
    "scm:git@github.com:beangle/commons.git"
  )
)

developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = uri("http://github.com/duantihua")
  )
)

description := "The Beangle Commons Library"
homepage := Some(uri("http://beangle.github.io/commons/index.html"))

lazy val root = (project in file("."))
  .settings(
    name := "beangle-commons",
    common,
    libraryDependencies ++= Seq(slf4j % "optional", logback_classic % "optional", logback_core % "optional"),
    libraryDependencies ++= Seq(jexl3 % "optional", apache_commons_compress % "optional"),
    libraryDependencies ++= Seq(jul_to_slf4j % "optional", scalatest),
    libraryDependencies += "com.swoval" % "file-tree-views" % "2.1.12" % "optional"
  )
