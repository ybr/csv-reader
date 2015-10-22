name := "csv-reader"

organization := "com.github.ybr"

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
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
  "org.scalaz" %% "scalaz-scalacheck-binding" % "7.1.4" % "test"
)

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials("Sonatype Nexus Repository Manager",
                            "oss.sonatype.org",
                            sys.props("sonatype.user"),
                            sys.props("sonatype.pass"))

pomExtra := (
  <url>https://github.com/ybr/csv-reader</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>https://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:ybr/csv-reader.git</url>
    <connection>scm:git:git@github.com:ybr/csv-reader.git</connection>
  </scm>
  <developers>
    <developer>
      <id>ybr</id>
      <name>Yohann Bredoux</name>
      <url>http://ybr.github.io</url>
    </developer>
  </developers>)