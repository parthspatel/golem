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

import cloud.golem.zio.agentic.AgentId
import zio.*

import java.util.UUID
import scala.concurrent.duration.FiniteDuration

/**
 * Error type for Golem host operations.
 */
sealed trait GolemError extends Throwable
object GolemError:
  case class PromiseError(message: String) extends GolemError
  case class TransactionError(message: String) extends GolemError
  case class RuntimeError(message: String) extends GolemError

/**
 * Represents a promise ID.
 */
final case class PromiseId(agentId: AgentId, oplogIdx: Long):
  override def toString: String = s"$agentId/$oplogIdx"

object PromiseId:
  def fromString(s: String): Either[String, PromiseId] =
    val lastSlash = s.lastIndexOf('/')
    if lastSlash < 0 then Left(s"Invalid promise ID format: $s")
    else
      val agentIdStr = s.substring(0, lastSlash)
      val oplogIdxStr = s.substring(lastSlash + 1)
      for
        agentId <- AgentId.fromString(agentIdStr)
        oplogIdx <- oplogIdxStr.toLongOption.toRight(s"Invalid oplog index: $oplogIdxStr")
      yield PromiseId(agentId, oplogIdx)

/**
 * Represents a promise that can be awaited and completed.
 */
trait Promise:
  def id: PromiseId
  def isCompleted: Boolean
  def get: IO[GolemError, Array[Byte]]

/**
 * Service for interacting with the Golem host.
 */
trait GolemHost:
  def createPromise: IO[GolemError, Promise]
  def awaitPromise(id: PromiseId): IO[GolemError, Array[Byte]]
  def completePromise(id: PromiseId, data: Array[Byte]): IO[GolemError, Unit]

  def getPersistenceLevel: UIO[PersistenceLevel]
  def setPersistenceLevel(level: PersistenceLevel): UIO[Unit]
  def getIdempotenceMode: UIO[Boolean]
  def setIdempotenceMode(mode: Boolean): UIO[Unit]
  def getRetryPolicy: UIO[RetryPolicy]
  def setRetryPolicy(policy: RetryPolicy): UIO[Unit]

  def generateIdempotencyKey: UIO[UUID]

object GolemHost:
  // Accessor methods
  def createPromise: ZIO[GolemHost, GolemError, Promise] =
    ZIO.serviceWithZIO(_.createPromise)

  def awaitPromise(id: PromiseId): ZIO[GolemHost, GolemError, Array[Byte]] =
    ZIO.serviceWithZIO(_.awaitPromise(id))

  def completePromise(id: PromiseId, data: Array[Byte]): ZIO[GolemHost, GolemError, Unit] =
    ZIO.serviceWithZIO(_.completePromise(id, data))

  def getPersistenceLevel: ZIO[GolemHost, Nothing, PersistenceLevel] =
    ZIO.serviceWithZIO(_.getPersistenceLevel)

  def setPersistenceLevel(level: PersistenceLevel): ZIO[GolemHost, Nothing, Unit] =
    ZIO.serviceWithZIO(_.setPersistenceLevel(level))

  def getIdempotenceMode: ZIO[GolemHost, Nothing, Boolean] =
    ZIO.serviceWithZIO(_.getIdempotenceMode)

  def setIdempotenceMode(mode: Boolean): ZIO[GolemHost, Nothing, Unit] =
    ZIO.serviceWithZIO(_.setIdempotenceMode(mode))

  def getRetryPolicy: ZIO[GolemHost, Nothing, RetryPolicy] =
    ZIO.serviceWithZIO(_.getRetryPolicy)

  def setRetryPolicy(policy: RetryPolicy): ZIO[GolemHost, Nothing, Unit] =
    ZIO.serviceWithZIO(_.setRetryPolicy(policy))

  def generateIdempotencyKey: ZIO[GolemHost, Nothing, UUID] =
    ZIO.serviceWithZIO(_.generateIdempotencyKey)

  // Guard methods
  def usePersistenceLevel[R, E, A](level: PersistenceLevel)(
      effect: ZIO[R, E, A]
  ): ZIO[R & GolemHost, E, A] =
    ZIO.scoped {
      for
        original <- getPersistenceLevel
        _        <- setPersistenceLevel(level)
        result   <- effect.ensuring(setPersistenceLevel(original))
      yield result
    }

  def useIdempotenceMode[R, E, A](mode: Boolean)(
      effect: ZIO[R, E, A]
  ): ZIO[R & GolemHost, E, A] =
    ZIO.scoped {
      for
        original <- getIdempotenceMode
        _        <- setIdempotenceMode(mode)
        result   <- effect.ensuring(setIdempotenceMode(original))
      yield result
    }

  def useRetryPolicy[R, E, A](policy: RetryPolicy)(
      effect: ZIO[R, E, A]
  ): ZIO[R & GolemHost, E, A] =
    ZIO.scoped {
      for
        original <- getRetryPolicy
        _        <- setRetryPolicy(policy)
        result   <- effect.ensuring(setRetryPolicy(original))
      yield result
    }

  // Live implementation (stub for now)
  val live: ULayer[GolemHost] = ZLayer.succeed(new GolemHost:
    private var persistenceLevel: PersistenceLevel = PersistenceLevel.Smart
    private var idempotenceMode: Boolean = true
    private var retryPolicy: RetryPolicy = RetryPolicy.defaults

    def createPromise: IO[GolemError, Promise] =
      ZIO.succeed(new Promise:
        val id = PromiseId(
          AgentId(
            cloud.golem.zio.agentic.ComponentId.random,
            "current-agent"
          ),
          0L
        )
        var isCompleted: Boolean = false
        var result: Array[Byte] = Array.empty

        def get: IO[GolemError, Array[Byte]] =
          if isCompleted then ZIO.succeed(result)
          else ZIO.fail(GolemError.PromiseError("Promise not completed"))
      )

    def awaitPromise(id: PromiseId): IO[GolemError, Array[Byte]] =
      ZIO.fail(GolemError.RuntimeError("Not implemented in stub"))

    def completePromise(id: PromiseId, data: Array[Byte]): IO[GolemError, Unit] =
      ZIO.unit

    def getPersistenceLevel: UIO[PersistenceLevel] = ZIO.succeed(persistenceLevel)
    def setPersistenceLevel(level: PersistenceLevel): UIO[Unit] =
      ZIO.succeed { persistenceLevel = level }

    def getIdempotenceMode: UIO[Boolean] = ZIO.succeed(idempotenceMode)
    def setIdempotenceMode(mode: Boolean): UIO[Unit] =
      ZIO.succeed { idempotenceMode = mode }

    def getRetryPolicy: UIO[RetryPolicy] = ZIO.succeed(retryPolicy)
    def setRetryPolicy(policy: RetryPolicy): UIO[Unit] =
      ZIO.succeed { retryPolicy = policy }

    def generateIdempotencyKey: UIO[UUID] = ZIO.succeed(UUID.randomUUID())
  )
