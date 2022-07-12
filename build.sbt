ThisBuild / tlBaseVersion := "0.0"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2022)
ThisBuild / tlSonatypeUseLegacyHost := false

ThisBuild / crossScalaVersions := Seq("2.12.16")

lazy val root = tlCrossRootProject.aggregate(bundlemon)

lazy val bundlemon = project
  .in(file("bundlemon"))
  .settings(
    name := "sbt-bundlemon"
  )
  .enablePlugins(SbtPlugin)
