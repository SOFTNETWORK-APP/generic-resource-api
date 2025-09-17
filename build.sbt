ThisBuild / organization := "app.softnetwork"

name := "resource"

ThisBuild / version := "0.8.0"

ThisBuild / scalaVersion := "2.12.18"

ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature", "-target:jvm-1.8", "-Ypartial-unification")

ThisBuild / javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

ThisBuild / resolvers ++= Seq(
  "Softnetwork Server" at "https://softnetwork.jfrog.io/artifactory/releases/",
  "Maven Central Server" at "https://repo1.maven.org/maven2",
  "Typesafe Server" at "https://repo.typesafe.com/typesafe/releases"
)

ThisBuild / versionScheme := Some("early-semver")

val scalatest = Seq(
  "org.scalatest" %% "scalatest" % Versions.scalatest  % Test
)

ThisBuild / libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.1"
) ++ scalatest

Test / parallelExecution := false

lazy val common = project.in(file("common"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .enablePlugins(AkkaGrpcPlugin)

lazy val local = project.in(file("local"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .dependsOn(
    common % "compile->compile;test->test;it->it"
  )

lazy val core = project.in(file("core"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings, app.softnetwork.Info.infoSettings)
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(
    common % "compile->compile;test->test;it->it"
  )

lazy val testkit = project.in(file("testkit"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .dependsOn(
    core % "compile->compile;test->test;it->it",
    local % "compile->compile;test->test;it->it"
  )

lazy val api = project.in(file("api"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .dependsOn(
    core % "compile->compile;test->test;it->it"
  )

lazy val root = project.in(file("."))
  .aggregate(common, local, core, testkit, api)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
