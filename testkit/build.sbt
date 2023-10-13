Test / parallelExecution := false

organization := "app.softnetwork.resource"

name := "resource-testkit"

libraryDependencies ++= Seq(
  "app.softnetwork.api" %% "generic-server-api-testkit" % Versions.genericPersistence,
  "app.softnetwork.session" %% "session-testkit" % Versions.genericPersistence,
  "org.scalatest" %% "scalatest" % Versions.scalatest
)
