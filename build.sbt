import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

organization := "org.beangle.commons"
version := "6.3.0"
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
    libraryDependencies += "com.swoval" % "file-tree-views" % "2.1.12" % "optional",
    // GraalVM AOT 配置改为手工生成并提交（src/main/resources/META-INF/native-image/beangle），
    // 构建期不再自动运行生成器，避免 sbt 2.0.8 下 AotPlugin 的偶发失败；需要更新时执行 `sbt aotGenerate`。
    Compile / aotHints := Def.uncached { Seq.empty[File] }
  )

/** 手工重新生成 GraalVM AOT 配置并写入 src/main/resources/META-INF/native-image/beangle。 */
lazy val aotGenerate = taskKey[Unit]("Regenerate GraalVM AOT configs into src/main/resources")

aotGenerate := Def.uncached {
  given FileConverter = fileConverter.value
  val cp = (Runtime / fullClasspath).value.files.map(_.toAbsolutePath.toString)
  val registrars = (Compile / resourceDirectory).value / "META-INF/beangle/aot-registrars.txt"
  val outDir = (Compile / resourceDirectory).value / "META-INF/native-image/beangle"
  val cmd = Seq("java", "-cp", cp.mkString(java.io.File.pathSeparator),
    "org.beangle.commons.aot.AotHintGenerator",
    "--registrars", registrars.getAbsolutePath,
    "--output", outDir.getAbsolutePath) ++ cp
  val exitCode = scala.sys.process.Process(cmd).!(streams.value.log)
  if (exitCode != 0) sys.error(s"aotGenerate failed with exit code $exitCode")
}
