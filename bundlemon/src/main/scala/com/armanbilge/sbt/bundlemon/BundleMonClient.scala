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

package com.armanbilge.sbt.bundlemon

import cats.effect.Concurrent
import cats.syntax.all._
import io.circe.Encoder
import io.circe.Json
import org.http4s.EntityEncoder
import org.http4s.Headers
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.http4s.circe.CirceEntityDecoder
import org.http4s.circe.CirceInstances
import org.http4s.client.Client

trait BundleMonClient[F[_]] {

  def getOrCreateProjectId(payload: GitDetails): F[Project]

  def createCommitRecord(
      projectId: String,
      payload: CommitRecordPayload
  ): F[CreateCommitRecordResponse]

  def createGithubOutput(
      projectId: String,
      commitRecordId: String,
      payload: GithubOutputPayload
  ): F[Unit]

}

object BundleMonClient {

  def apply[F[_]: Concurrent](
      client: Client[F],
      endpoint: Uri,
      runId: String,
      commitSha: String
  ): BundleMonClient[F] = {

    val headers = clientHeaders
    val baseUri = endpoint / "v1"

    new BundleMonClient[F] with CirceInstances with CirceEntityDecoder {

      override protected val defaultPrinter = super.defaultPrinter.copy(dropNullValues = true)

      implicit def circeEntityEncoder[A: Encoder]: EntityEncoder[F, A] =
        jsonEncoderOf[F, A]

      def getOrCreateProjectId(payload: GitDetails): F[Project] = {
        val uri =
          baseUri / "projects" / "id" +? ("runId" -> runId) +? ("commitSha" -> commitSha)
        client.expect(Request[F](Method.POST, uri, headers = headers).withEntity(payload))
      }

      def createCommitRecord(
          projectId: String,
          payload: CommitRecordPayload
      ): F[CreateCommitRecordResponse] = {
        val uri =
          baseUri / "projects" / projectId / "commit-records" +? ("authType" -> "GITHUB_ACTIONS") +? ("runId" -> runId)
        client.expect(
          Request[F](Method.POST, uri, headers = headers).withEntity(payload)
        )
      }

      def createGithubOutput(
          projectId: String,
          commitRecordId: String,
          payload: GithubOutputPayload
      ): F[Unit] = {
        val uri =
          baseUri / "projects" / projectId / "commit-records" / commitRecordId / "outputs" / "github"
        client
          .expect[Json](
            Request[F](Method.POST, uri, headers = headers).withEntity(payload)
          )(jsonDecoder)
          .void
      }

    }
  }

  private val clientHeaders =
    Headers("x-api-client-name" -> BuildInfo.name, "x-api-client-version" -> BuildInfo.version)

}
