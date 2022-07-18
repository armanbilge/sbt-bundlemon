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

import io.circe.Decoder

import GithubEvent._
import PullRequest._

final case class GithubEvent(pullRequest: PullRequest)

object GithubEvent {

  implicit val decoder: Decoder[GithubEvent] =
    Decoder.forProduct1("pull_request")(GithubEvent.apply)

  final case class PullRequest(head: Head)

  object PullRequest {
    implicit val decoder: Decoder[PullRequest] = Decoder.forProduct1("head")(PullRequest.apply)

    final case class Head(sha: String)
    object Head {
      implicit val decoder: Decoder[Head] = Decoder.forProduct1("sha")(Head.apply)
    }
  }
}
