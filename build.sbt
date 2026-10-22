ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "transfer"
ThisBuild / version := "0.1.0"
ThisBuild / libraryDependencies ++= Seq(
  "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test,
  "org.assertj" % "assertj-core" % "3.26.3" % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "data-transfer",
    crossPaths := false,
    autoScalaLibrary := false,
    javacOptions ++= Seq("--release", "17"),
    Compile / mainClass := Some("transfer.Main"),
    Compile / run / fork := true,
    Test / testOptions += Tests.Argument(jupiterTestFramework, "-v")
  )

lazy val it = (project in file("it"))
  .dependsOn(root)
  .settings(
    name := "data-transfer-it",
    crossPaths := false,
    autoScalaLibrary := false,
    javacOptions ++= Seq("--release", "17"),
    Test / testOptions += Tests.Argument(jupiterTestFramework, "-v"),
    publish / skip := true
  )
