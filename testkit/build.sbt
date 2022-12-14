import app.softnetwork.sbt.build.Versions

Test / parallelExecution := false

organization := "app.softnetwork.resource"

name := "resource-testkit"

libraryDependencies ++= Seq(
  "app.softnetwork.persistence" %% "persistence-session-testkit" % Versions.genericPersistence
)
