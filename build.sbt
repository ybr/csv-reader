name := """csv"""

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.4",
  "joda-time" % "joda-time" % "2.8.2",
  "org.joda" % "joda-convert" % "1.8.1",
  "org.scala-lang" % "scala-reflect" % "2.11.7"
)

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.5" % Test