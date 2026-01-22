ThisBuild / version := "0.0.0"
ThisBuild / organization := "cloud.golem"
ThisBuild / scalaVersion := "2.13.12"

// Publishing settings
ThisBuild / homepage := Some(url("https://github.com/golemcloud/golem"))
ThisBuild / licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / developers := List(
  Developer(
    "golemcloud",
    "Golem Cloud",
    "hello@golem.cloud",
    url("https://golem.cloud")
  )
)
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / publishMavenStyle := true
ThisBuild / publishTo := sonatypePublishToBundle.value

lazy val root = (project in file("."))
  .aggregate(golemSdk)
  .settings(
    name := "golem-java-sdk-root",
    publish / skip := true
  )

lazy val golemSdk = (project in file("golem-sdk"))
  .settings(
    name := "golem-sdk",
    description := "Java and Scala SDK for building Golem applications",

    // Java settings
    crossPaths := false, // Don't append Scala version to artifact name
    autoScalaLibrary := false, // Don't include Scala library by default

    // Compile Java sources
    Compile / javacOptions ++= Seq("-source", "17", "-target", "17"),

    // Include Scala library for Scala sources
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-library" % scalaVersion.value % Optional,

      // Test dependencies
      "org.junit.jupiter" % "junit-jupiter" % "5.10.1" % Test,
      "org.junit.jupiter" % "junit-jupiter-engine" % "5.10.1" % Test,
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    ),

    // Enable JUnit 5
    Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),

    // Source directories
    Compile / javaSource := baseDirectory.value / "src" / "main" / "java",
    Compile / scalaSource := baseDirectory.value / "src" / "main" / "scala",
    Test / javaSource := baseDirectory.value / "src" / "test" / "java",
    Test / scalaSource := baseDirectory.value / "src" / "test" / "scala"
  )
