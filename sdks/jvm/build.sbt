ThisBuild / version := "0.0.0"
ThisBuild / organization := "cloud.golem"

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

// Root project
lazy val root = (project in file("."))
  .aggregate(golemJava, golemScala, golemZio)
  .settings(
    name := "golem-jvm-sdk",
    publish / skip := true
  )

// ============================================================================
// Java SDK - Pure Java implementation
// ============================================================================
lazy val golemJava = (project in file("golem-java"))
  .settings(
    name := "golem-java",
    description := "Pure Java SDK for building Golem applications",

    // Pure Java project settings
    crossPaths := false,
    autoScalaLibrary := false,

    // Java compiler settings
    Compile / javacOptions ++= Seq("-source", "17", "-target", "17", "-Xlint:all"),
    Compile / doc / javacOptions := Seq("-source", "17"),

    libraryDependencies ++= Seq(
      "org.junit.jupiter" % "junit-jupiter" % "5.10.1" % Test,
      "org.junit.jupiter" % "junit-jupiter-engine" % "5.10.1" % Test
    ),

    Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
  )

// ============================================================================
// Scala SDK - Scala-idiomatic wrappers, depends on Java SDK
// ============================================================================
lazy val golemScala = (project in file("golem-scala"))
  .dependsOn(golemJava)
  .settings(
    name := "golem-scala",
    description := "Scala SDK for building Golem applications",
    scalaVersion := "3.3.1",

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    ),

    // Scala compiler settings
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings"
    )
  )

// ============================================================================
// ZIO SDK - ZIO-native effects, depends on Scala SDK
// ============================================================================
lazy val golemZio = (project in file("golem-zio"))
  .dependsOn(golemScala)
  .settings(
    name := "golem-zio",
    description := "ZIO-native SDK for building Golem applications",
    scalaVersion := "3.3.1",

    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.19",
      "dev.zio" %% "zio-streams" % "2.0.19",
      "dev.zio" %% "zio-json" % "0.6.2",
      "dev.zio" %% "zio-test" % "2.0.19" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.19" % Test
    ),

    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),

    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings"
    )
  )
