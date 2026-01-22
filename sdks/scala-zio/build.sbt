import sbt._
import sbt.Keys._

ThisBuild / version := "0.0.0"
ThisBuild / scalaVersion := "3.3.1"
ThisBuild / organization := "cloud.golem"
ThisBuild / organizationName := "Golem Cloud"
ThisBuild / homepage := Some(url("https://golem.cloud"))
ThisBuild / licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / developers := List(
  Developer(
    id = "golemcloud",
    name = "Golem Cloud",
    email = "contact@golem.cloud",
    url = url("https://golem.cloud")
  )
)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/golemcloud/golem"),
    "scm:git:git://github.com/golemcloud/golem.git"
  )
)

lazy val zioVersion = "2.0.21"
lazy val zioJsonVersion = "0.6.2"

lazy val root = (project in file("."))
  .settings(
    name := "golem-zio-sdk"
  )
  .aggregate(golemZio)

lazy val golemZio = (project in file("golem-zio"))
  .settings(
    name := "golem-zio",
    description := "Golem SDK for Scala with ZIO integration",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-json" % zioJsonVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
      "-language:higherKinds"
    )
  )
