import Dependencies._
import BuildSettings._
//import scalariform.formatter.preferences._

ThisBuild / organization := "org.beangle"
ThisBuild / organizationName  := "The Beangle Software"
ThisBuild / startYear := Some(2005)
ThisBuild / licenses += ("GNU Lesser General Public License version 3", new URL("http://www.gnu.org/licenses/lgpl-3.0.txt"))

lazy val root = (project in file("."))
  .settings()
  .aggregate(core,text,csv,dbf,commons_file)

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-commons-core",
    commonSettings,
    libraryDependencies ++= (commonDeps)
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