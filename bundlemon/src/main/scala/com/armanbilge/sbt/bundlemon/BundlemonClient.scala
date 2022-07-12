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
import org.http4s.Headers
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client

trait BundlemonClient[F[_]] {

  def createCommitRecord(payload: CommitRecordPayload): F[CreateCommitRecordResponse]

}

object BundlemonClient {

  sealed abstract class Auth
  final case class GithubActionsAuth(
      owner: String,
      repo: String,
      runId: String
  ) extends Auth

  def apply[F[_]: Concurrent](
      client: Client[F],
      endpoint: Uri,
      projectId: String,
      auth: Auth
  ): BundlemonClient[F] = {
    val authHeaders = auth match {
      case GithubActionsAuth(owner, repo, runId) =>
        Headers(
          "BundleMon-Auth-Type" -> "GITHUB_ACTION",
          "GitHub-Owner" -> owner,
          "GitHub-Repo" -> repo,
          "GitHub-Run-ID" -> runId
        )
    }

    val uri = endpoint / "v1" / "projects" / projectId / "commit-records"
    val request = Request[F](Method.POST, uri, headers = clientHeaders ++ authHeaders)

    payload => client.expect[CreateCommitRecordResponse](request.withEntity(payload))
  }

  private val clientHeaders =
    Headers("x-api-client-name" -> BuildInfo.name, "x-api-client-version" -> BuildInfo.version)

}
