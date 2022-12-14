import app.softnetwork.sbt.build._

organization := "app.softnetwork.resource"

name := "resource-common"

libraryDependencies ++= Seq(
  "app.softnetwork.api" %% "generic-server-api" % Versions.genericPersistence,
  "app.softnetwork.protobuf" %% "scalapb-extensions" % "0.1.5"
)

Compile / unmanagedResourceDirectories += baseDirectory.value / "src/main/protobuf"
