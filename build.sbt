import Dependencies._
import BuildSettings._
//import scalariform.formatter.preferences._

ThisBuild / organization := "org.beangle"
ThisBuild / organizationName  := "The Beangle Software"
ThisBuild / startYear := Some(2005)
ThisBuild / licenses += ("GNU Lesser General Public License version 3", new URL("http://www.gnu.org/licenses/lgpl-3.0.txt"))

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

ThisBuild / description := "The Beangle Commons Library."
ThisBuild / homepage := Some(url("http://beangle.github.io/commons/index.html"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
ThisBuild / publishConfiguration := publishConfiguration.value.withOverwrite(true)
ThisBuild / publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
ThisBuild / publishM2Configuration := publishM2Configuration.value.withOverwrite(true)
ThisBuild / versionScheme := Some("early-semver")

lazy val root = (project in file("."))
  .settings()
  .aggregate(core,text,csv,dbf,commons_file)

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-commons-core",
    commonSettings,
    libraryDependencies ++= (commonDeps),
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
  )

lazy val text = (project in file("text"))
  .settings(
    name := "beangle-commons-text",
    commonSettings,
    libraryDependencies ++= (commonDeps)
  ).dependsOn(core)

lazy val csv = (project in file("csv"))
  .settings(
    name := "beangle-commons-csv",
    commonSettings,
    libraryDependencies ++= (commonDeps)
  ).dependsOn(core)

lazy val dbf = (project in file("dbf"))
  .settings(
    name := "beangle-commons-dbf",
    commonSettings,
    libraryDependencies ++= (commonDeps)
  ).dependsOn(core)

lazy val commons_file = (project in file("file"))
  .settings(
    name := "beangle-commons-file",
    commonSettings,
    libraryDependencies ++= (commonDeps ++ Seq(commonsCompress))
  ).dependsOn(core)

publish / skip := true
