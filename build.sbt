ThisBuild / tlBaseVersion := "0.1"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2022)
ThisBuild / tlSonatypeUseLegacyHost := false

ThisBuild / crossScalaVersions := Seq("2.12.17")

val http4sVersion = "0.23.18"
val brotli4jVersion = "1.10.0"

lazy val root = tlCrossRootProject.aggregate(bundlemon)

lazy val bundlemon = project
  .in(file("bundlemon"))
  .settings(
    name := "sbt-bundlemon",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "com.aayushatharva.brotli4j" % "brotli4j" % brotli4jVersion,
      "com.aayushatharva.brotli4j" % "native-linux-x86_64" % brotli4jVersion
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
    },
    tlVersionIntroduced := Map("2.12" -> "0.1.3")
  )
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
