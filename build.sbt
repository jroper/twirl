lazy val twirl = project
  .in(file("."))
  .aggregate(api, parser, compiler)
  .settings(common: _*)
  .settings(crossScala: _*)
  .settings(noPublish: _*)

lazy val api = project
  .in(file("api"))
  .settings(common: _*)
  .settings(crossScala: _*)
  .settings(
    name := "twirl-api",
    libraryDependencies += commonsLang
  )

lazy val parser = project
  .in(file("parser"))
  .settings(common: _*)
  .settings(crossScala: _*)
  .settings(
    name := "twirl-parser",
    libraryDependencies += specs2(scalaBinaryVersion.value),
    libraryDependencies += scalaIO(scalaBinaryVersion.value) % "test"
  )

lazy val compiler = project
  .in(file("compiler"))
  .dependsOn(api, parser % "compile;test->test")
  .settings(common: _*)
  .settings(crossScala: _*)
  .settings(
    name := "twirl-compiler",
    libraryDependencies += scalaCompiler(scalaVersion.value),
    libraryDependencies += scalaIO(scalaBinaryVersion.value)
  )

lazy val plugin = project
  .in(file("sbt-twirl"))
  .dependsOn(compiler)
  .settings(common: _*)
  .settings(scriptedSettings: _*)
  .settings(
    name := "sbt-twirl",
    organization := "com.typesafe.sbt",
    sbtPlugin := true,
    scriptedLaunchOpts += ("-Dproject.version=" + version.value),
    scriptedLaunchOpts += "-XX:MaxPermSize=256m",
    resourceGenerators in Compile <+= generateVersionFile
  )

// Shared settings

def common = Seq(
  organization := "com.typesafe.twirl",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.10.4",
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
)

def crossScala = Seq(
  crossScalaVersions := Seq("2.9.3", "2.10.4"),
  unmanagedSourceDirectories in Compile += (sourceDirectory in Compile).value / ("scala-" + scalaBinaryVersion.value)
)

def noPublish = Seq(
  publish := {},
  publishLocal := {}
)

// Version file

def generateVersionFile = Def.task {
  val version = (Keys.version in api).value
  val file = (resourceManaged in Compile).value / "twirl.version.properties"
  val content = s"twirl.api.version=$version"
  IO.write(file, content)
  Seq(file)
}

// Dependencies

def commonsLang = "org.apache.commons" % "commons-lang3" % "3.1"

def scalaCompiler(version: String) = "org.scala-lang" % "scala-compiler" % version

def scalaIO(scalaVersion: String) = scalaVersion match {
  case "2.9.3" => "com.github.scala-incubator.io" % "scala-io-file_2.9.2"  % "0.4.1-seq"
  case "2.10" => "com.github.scala-incubator.io" %% "scala-io-file"  % "0.4.2"
}

def specs2(scalaBinaryVersion: String) = scalaBinaryVersion match {
  case "2.9.3" => "org.specs2" %% "specs2" % "1.12.4.1" % "test"
  case "2.10"  => "org.specs2" %% "specs2" % "2.3.10" % "test"
}
