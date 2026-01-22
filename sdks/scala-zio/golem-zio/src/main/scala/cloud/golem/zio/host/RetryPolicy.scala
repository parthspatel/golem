/*
 * Copyright 2024-2025 Golem Cloud
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

package cloud.golem.zio.host

import zio.*
import zio.json.*

import scala.concurrent.duration.{Duration as ScalaDuration, *}

/**
 * Persistence level for oplog entries.
 */
enum PersistenceLevel:
  case PersistNothing
  case PersistRemoteSideEffects
  case Smart

object PersistenceLevel:
  given JsonEncoder[PersistenceLevel] = JsonEncoder.string.contramap {
    case PersistNothing           => "persist_nothing"
    case PersistRemoteSideEffects => "persist_remote_side_effects"
    case Smart                    => "smart"
  }

  given JsonDecoder[PersistenceLevel] = JsonDecoder.string.map {
    case "persist_nothing"             => PersistNothing
    case "persist_remote_side_effects" => PersistRemoteSideEffects
    case "smart"                       => Smart
  }

/**
 * Configuration for retry behavior on failures.
 *
 * @param maxAttempts Maximum number of retry attempts
 * @param minDelay Minimum delay between retries
 * @param maxDelay Maximum delay between retries
 * @param multiplier Multiplier for exponential backoff
 * @param maxJitterFactor Optional jitter factor (0.0 to 1.0)
 */
final case class RetryPolicy(
    maxAttempts: Int,
    minDelay: Duration,
    maxDelay: Duration,
    multiplier: Double,
    maxJitterFactor: Option[Double] = None
)

object RetryPolicy:
  /**
   * Create a default retry policy.
   */
  def defaults: RetryPolicy =
    RetryPolicy(
      maxAttempts = 3,
      minDelay = 1.second,
      maxDelay = 60.seconds,
      multiplier = 2.0,
      maxJitterFactor = None
    )

  /**
   * Create a policy that doesn't retry.
   */
  def noRetry: RetryPolicy =
    RetryPolicy(
      maxAttempts = 1,
      minDelay = Duration.Zero,
      maxDelay = Duration.Zero,
      multiplier = 1.0,
      maxJitterFactor = None
    )

  /**
   * Create an exponential backoff retry policy.
   */
  def exponentialBackoff(
      maxAttempts: Int = 5,
      minDelay: Duration = 1.second,
      maxDelay: Duration = 60.seconds,
      multiplier: Double = 2.0
  ): RetryPolicy =
    RetryPolicy(maxAttempts, minDelay, maxDelay, multiplier, None)

  /**
   * Convert to ZIO Schedule.
   */
  extension (policy: RetryPolicy)
    def toSchedule[E]: Schedule[Any, E, Long] =
      Schedule
        .exponential(policy.minDelay)
        .whileOutput(_ < policy.maxDelay)
        .upTo(policy.maxDelay * policy.maxAttempts)
        .jittered(policy.maxJitterFactor.getOrElse(0.0), policy.maxJitterFactor.getOrElse(0.0))
        .recurs(policy.maxAttempts - 1)
