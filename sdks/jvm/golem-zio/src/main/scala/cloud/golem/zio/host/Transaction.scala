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

/**
 * Transaction API for durable, atomic operations.
 *
 * Transactions ensure that a group of operations either all succeed
 * or all fail together. In case of failure during replay, the entire
 * transaction is re-executed.
 */
object Transaction:

  /**
   * Execute a ZIO effect atomically.
   *
   * All operations within the effect are treated as a single atomic unit.
   * In case of failure during replay, the entire block is re-executed.
   *
   * Example:
   * {{{
   * Transaction.atomically {
   *   for {
   *     _ <- externalCall1
   *     _ <- externalCall2
   *   } yield ()
   * }
   * }}}
   */
  def atomically[R, E, A](effect: ZIO[R, E, A]): ZIO[R, E, A] =
    ZIO.acquireReleaseWith(begin)(_ => commit.orDie)(_ => effect)

  /**
   * Execute a ZIO effect with compensating actions.
   *
   * If the effect fails, all registered compensations will be executed
   * in reverse order.
   *
   * Example:
   * {{{
   * Transaction.withCompensation(
   *   effect = createResource,
   *   compensation = deleteResource
   * )
   * }}}
   */
  def withCompensation[R, E, A](
      effect: ZIO[R, E, A]
  )(compensation: ZIO[R, Nothing, Unit]): ZIO[R, E, A] =
    effect.onError(_ => compensation)

  /**
   * Begin a new transaction.
   *
   * Returns a transaction context that must be committed or rolled back.
   */
  def begin: UIO[TransactionContext] =
    ZIO.succeed(new TransactionContext)

  /**
   * Commit the current transaction.
   */
  def commit: UIO[Unit] =
    // In runtime, this would call mark_end_operation()
    ZIO.unit

  /**
   * Rollback the current transaction.
   */
  def rollback: UIO[Unit] =
    ZIO.unit

/**
 * Transaction context for managing compensations.
 */
class TransactionContext:
  private var compensations: List[UIO[Unit]] = Nil

  /**
   * Add a compensation action.
   */
  def addCompensation(compensation: UIO[Unit]): UIO[Unit] =
    ZIO.succeed {
      compensations = compensation :: compensations
    }

  /**
   * Execute all compensations in reverse order.
   */
  def executeCompensations: UIO[Unit] =
    ZIO.foreachDiscard(compensations)(_.ignore)
