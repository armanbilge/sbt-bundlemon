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

import io.circe.Codec
import io.circe.Encoder
import io.circe.Decoder

final case class GitDetails(provider: String, owner: String, repo: String)

object GitDetails {
  implicit def encoder: Encoder[GitDetails] =
    Encoder.forProduct3("provider", "owner", "repo")(gd => (gd.provider, gd.owner, gd.repo))
}

final case class Project(id: String)

object Project {
  implicit def decoder: Decoder[Project] = Decoder.forProduct1("id")(Project.apply)
}

final case class FileDetails(
    friendlyName: String,
    pattern: String,
    path: String,
    size: Long,
    compression: String,
    maxSize: Option[Long],
    maxPercentIncrease: Option[Double]
)

object FileDetails {
  implicit val codec: Codec[FileDetails] = Codec.forProduct7(
    "friendlyName",
    "pattern",
    "path",
    "size",
    "compression",
    "maxSize",
    "maxPercentIncrease"
  )(FileDetails.apply)(fd =>
    (
      fd.friendlyName,
      fd.pattern,
      fd.path,
      fd.size,
      fd.compression,
      fd.maxSize,
      fd.maxPercentIncrease
    )
  )
}

final case class CommitRecordPayload(
    subProject: String,
    files: List[FileDetails],
    groups: List[FileDetails],
    branch: String,
    commitSha: String,
    baseBranch: Option[String],
    prNumber: Option[String]
)

object CommitRecordPayload {
  implicit val encoder: Encoder[CommitRecordPayload] = Encoder.forProduct7(
    "subProject",
    "files",
    "groups",
    "branch",
    "commitSha",
    "baseBranch",
    "prNumber"
  )(cr =>
    (
      cr.subProject,
      cr.files,
      cr.groups,
      cr.branch,
      cr.commitSha,
      cr.baseBranch,
      cr.prNumber
    )
  )
}

final case class CreateCommitRecordResponse(record: CommitRecord)

object CreateCommitRecordResponse {
  implicit val codec: Codec[CreateCommitRecordResponse] =
    Codec.forProduct1("record")(CreateCommitRecordResponse.apply)(
      _.record
    )
}

final case class CommitRecord(id: String)

object CommitRecord {
  implicit val codec: Codec[CommitRecord] =
    Codec.forProduct1("id")(CommitRecord.apply)(_.id)
}

final case class GithubOutputPayload(
    git: GithubCommitInfo,
    output: GithubOutputOptions,
    auth: GithubAuth
)

object GithubOutputPayload {
  implicit val encoder: Encoder[GithubOutputPayload] =
    Encoder.forProduct3("git", "output", "auth")(gop => (gop.git, gop.output, gop.auth))
}

final case class GithubCommitInfo(
    owner: String,
    repo: String,
    commitSha: String,
    prNumber: Option[String]
)

object GithubCommitInfo {
  implicit val codec: Codec[GithubCommitInfo] =
    Codec.forProduct4("owner", "repo", "commitSha", "prNumber")(
      GithubCommitInfo.apply
    )(gci => (gci.owner, gci.repo, gci.commitSha, gci.prNumber))
}

final case class GithubOutputOptions(
    checkRun: Boolean,
    commitStatus: Boolean,
    prComment: Boolean
)

object GithubOutputOptions {
  implicit def encoder: Encoder[GithubOutputOptions] =
    Encoder.forProduct3("checkRun", "commitStatus", "prComment")(goo =>
      (goo.checkRun, goo.commitStatus, goo.prComment)
    )
}

final case class GithubAuth(runId: String)

object GithubAuth {
  implicit def encoder: Encoder[GithubAuth] =
    Encoder.forProduct1("runId")(_.runId)
}
