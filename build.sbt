

val scalaV = "2.12.4"

val akkaHttpV = "10.0.11"
val akkaV = "2.5.8"
val upickleV = "0.4.4"
val utestV = "0.4.5"
val specs2V = "4.0.2"
val scalaJsDomV = "0.9.1"
val boopickleV = "1.2.6"

lazy val root =
  project.in(file("."))
    .aggregate(backend)
    .settings(commonSettings: _*)
    .settings(
      mainClass in Compile := Some("com.hwg.Hwg"),
      name := "HWG",
      organization := "com.hwg",
      version := "0.1",
    )
    .enablePlugins(JavaAppPackaging)
    .dependsOn(backend)

updateConfiguration in updateSbtClassifiers := (updateConfiguration in updateSbtClassifiers).value.withMissingOk(true)

lazy val vecmath = ProjectRef(uri("git://github.com/patches11/vecmath.git#0010d1f062d8295fd78d1e98091cba2c85265196"), "vecMathJS")

// Scala-Js frontend
lazy val frontend =
  project.in(file("frontend"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      persistLauncher in Compile := true,
      persistLauncher in Test := false,
      relativeSourceMaps in Compile := true,
      testFrameworks += new TestFramework("utest.runner.Framework"),
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % scalaJsDomV,
        "io.suzaku" %%% "boopickle" % boopickleV,
        "com.lihaoyi" %%% "utest" % utestV % "test",
        "io.monix" %%% "monix" % "2.2.1"
      )
    )
    .dependsOn(sharedJs, vecmath)

// Akka Http based backend
lazy val backend =
  project.in(file("backend"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-stream" % akkaV % "runtime",
        "com.typesafe.akka" %% "akka-http" % akkaHttpV,
        "org.specs2" %% "specs2-core" % specs2V % "test",
        "io.suzaku" %% "boopickle" % boopickleV
      ),
      resourceGenerators in Compile += Def.task {
        val f1 = (fastOptJS in Compile in frontend).value
        val f2 = (packageScalaJSLauncher in Compile in frontend).value
        Seq(f1.data, f2.data)
      }.taskValue,
      watchSources ++= (watchSources in frontend).value
    )
    .dependsOn(sharedJvm)

val enumeratumVersion = "1.5.11"
val enumeratumUPickleVersion = "1.5.11"

lazy val shared =
  (crossProject.crossType(CrossType.Pure) in file ("shared"))
    .settings(
      scalaVersion := scalaV,
      autoCompilerPlugins := true,
      libraryDependencies ++= Seq(
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
        "com.thoughtworks.enableIf" %% "enableif" % "latest.release",
        "com.beachape" %% "enumeratum" % enumeratumVersion
      )
    )


lazy val sharedJvm= shared.jvm
lazy val sharedJs= shared.js

def commonSettings = Seq(
  scalaVersion := scalaV,
  scalacOptions ++= Seq("-deprecation", "-feature", "-encoding", "utf8", "-Ywarn-dead-code", "-unchecked", "-Xlint", "-Ywarn-unused-import")
)
