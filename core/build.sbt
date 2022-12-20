import app.softnetwork.sbt.build._

organization := "app.softnetwork.resource"

name := "resource-core"

libraryDependencies ++= Seq(
  "app.softnetwork.session" %% "session-core" % Versions.session
//  "com.google.cloud" % "google-cloud-storage" % "2.6.1",
//  "com.github.seratch" %% "awscala-s3" % "0.9.+"    
)

