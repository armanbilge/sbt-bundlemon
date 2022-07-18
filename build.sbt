ThisBuild / tlBaseVersion := "0.0"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2022)
ThisBuild / tlSonatypeUseLegacyHost := false

ThisBuild / crossScalaVersions := Seq("2.12.16")

val http4sVersion = "0.23.13"

ThisBuild / githubWorkflowEnv += "BUNDLEMON_PROJECT_ID" -> "${{ secrets.BUNDLEMON_PROJECT_ID }}"

lazy val root = tlCrossRootProject.aggregate(bundlemon)

lazy val bundlemon = project
  .in(file("bundlemon"))
  .settings(
    name := "sbt-bundlemon",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion
    ),
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.10.1"),
    buildInfoPackage := "com.armanbilge.sbt.bundlemon",
    buildInfoOptions += BuildInfoOption.PackagePrivate,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    Test / test := {
      scripted.toTask("").value
    }
  )
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
