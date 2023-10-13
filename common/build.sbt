organization := "app.softnetwork.resource"

name := "resource-common"

libraryDependencies ++= Seq(
  "app.softnetwork.api" %% "generic-server-api" % Versions.genericPersistence,
  "app.softnetwork.protobuf" %% "scalapb-extensions" % "0.1.7"
)

Compile / unmanagedResourceDirectories += baseDirectory.value / "src/main/protobuf"
