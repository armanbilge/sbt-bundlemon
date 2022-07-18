/*
 * Copyright 2022 Arman Bilge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.armanbilge.sbt

import com.armanbilge.sbt.bundlemon._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.all._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.http4s.client.middleware.RequestLogger
import org.http4s.client.middleware.ResponseLogger
import sbt._

import Keys._
import ScalaJSPlugin.autoImport._

object BundleMonPlugin extends AutoPlugin {

  override def requires: Plugins = ScalaJSPlugin

  object autoImport {
    lazy val bundleMonMaxSize = settingKey[Map[String, Long]]("max size per file (default: {})")
    lazy val bundleMonMaxPercentIncrease =
      settingKey[Map[String, Double]]("max percent increase per file (default: {})")
    lazy val bundleMonCheckRun = settingKey[Boolean]("check run (default: false)")
    lazy val bundleMonCommitStatus = settingKey[Boolean]("commit status (default: true)")
    lazy val bundleMonPrComment = settingKey[Boolean]("pr comment (default: true)")

    lazy val bundleMon = taskKey[Unit]("Calculate gzipped size and submit to bundleMon")
  }

  import autoImport._

  override lazy val buildSettings: Seq[Setting[_]] = Seq(
    bundleMonMaxSize := Map.empty,
    bundleMonMaxPercentIncrease := Map.empty,
    bundleMonCheckRun := false,
    bundleMonCommitStatus := true,
    bundleMonPrComment := true
  )

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    Compile / scalaJSStage := FullOptStage,
    Compile / bundleMon := {
      val maxSize = bundleMonMaxSize.value
      val maxPercentIncrease = bundleMonMaxPercentIncrease.value

      val outputDir =
        (Compile / fullLinkJS / scalaJSLinkerOutputDirectory)
          .value
          .relativeTo(baseDirectory.value)
          .get

      val files = (Compile / fullLinkJS).value.data.publicModules.map { module =>
        outputDir / module.jsFileName
      }

      val fileDetails = files.map { file =>
        val size = io.Using.fileInputStream(file) { in =>
          var size = 0L
          IO.gzip(in, _ => size += 1)
          size
        }

        FileDetails(
          file.getName,
          "**/*.js",
          file.getPath,
          size,
          "gzip",
          maxSize.get(file.getName),
          maxPercentIncrease.get(file.getName)
        )
      }.toList

      val modName = moduleName.value
      val subProject = CrossVersion(
        crossVersion.value,
        scalaVersion.value,
        scalaBinaryVersion.value
      ).fold(modName)(_.apply(modName))

      val Array(owner, repo) = System.getenv("GITHUB_REPOSITORY").split('/')

      val isPr = System.getenv("GITHUB_EVENT_NAME") == "pull_request"
      val ref = System.getenv("GITHUB_REF").split('/')

      val prNumber = if (isPr) Some(ref(2)) else None
      val commitSha = System.getenv("GITHUB_SHA")

      val commitRecordPayload = CommitRecordPayload(
        subProject,
        fileDetails,
        Nil,
        if (isPr) System.getenv("GITHUB_HEAD_REF") else ref.drop(2).mkString("/"),
        commitSha,
        Option(System.getenv("GITHUB_BASE_REF")),
        prNumber
      )

      val commitInfo = GithubCommitInfo(owner, repo, commitSha, prNumber)
      val outputOptions = GithubOutputOptions(
        bundleMonCheckRun.value,
        bundleMonCommitStatus.value,
        bundleMonPrComment.value
      )

      val outputPayload = GithubOutputPayload(commitInfo, outputOptions)

      val log = streams.value.log

      EmberClientBuilder
        .default[cats.effect.IO]
        .build
        .map(RequestLogger(false, true, logAction = Some(s => cats.effect.IO(log.info(s)))))
        .map(ResponseLogger(false, true, logAction = Some(s => cats.effect.IO(log.info(s)))))
        .use { ember =>
          BundleMonClient.GithubActionsAuth.fromEnv[cats.effect.IO].flatMap { auth =>
            val client = BundleMonClient(
              ember,
              uri"https://api.bundlemon.dev",
              System.getenv("BUNDLEMON_PROJECT_ID"),
              auth.get
            )

            client.createCommitRecord(commitRecordPayload) *>
              client.createGithubOutput(outputPayload)
          }
        }
        .unsafeRunSync()(cats.effect.unsafe.IORuntime.global)
    }
  )

}
