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

package cloud.golem.scala.host

import cloud.golem.java.host.{Transaction as JTransaction}
import scala.util.{Try, Success, Failure}

/**
 * Scala-idiomatic transaction API.
 *
 * Provides functional wrappers around the Java transaction API.
 */
object Transaction:

  /**
   * Execute a block of code atomically.
   *
   * If the block throws an exception, the transaction is rolled back.
   *
   * Example:
   * {{{
   * val result = Transaction.atomically {
   *   externalCall1()
   *   externalCall2()
   * }
   * }}}
   *
   * @param block The code to execute atomically
   * @return The result of the block
   */
  def atomically[T](block: => T): T =
    JTransaction.atomically(() => block)

  /**
   * Execute a block of code atomically, returning a Try.
   *
   * Example:
   * {{{
   * val result: Try[Int] = Transaction.tryAtomically {
   *   riskyOperation()
   * }
   * }}}
   *
   * @param block The code to execute atomically
   * @return Success with the result, or Failure with the exception
   */
  def tryAtomically[T](block: => T): Try[T] =
    Try(atomically(block))

  /**
   * Execute a block of code atomically, returning an Either.
   *
   * Example:
   * {{{
   * val result: Either[Throwable, Int] = Transaction.eitherAtomically {
   *   riskyOperation()
   * }
   * }}}
   *
   * @param block The code to execute atomically
   * @return Right with the result, or Left with the exception
   */
  def eitherAtomically[T](block: => T): Either[Throwable, T] =
    tryAtomically(block).toEither

  /**
   * Begin a transaction manually.
   *
   * Returns an AutoCloseable that will commit on successful close
   * or rollback on exception.
   *
   * Example:
   * {{{
   * val tx = Transaction.begin()
   * try
   *   operation1()
   *   operation2()
   *   tx.commit()
   * catch
   *   case e: Exception =>
   *     tx.rollback()
   *     throw e
   * }}}
   *
   * @return A transaction handle
   */
  def begin(): TransactionHandle =
    new TransactionHandle(JTransaction.begin())

/**
 * Handle for manual transaction management.
 */
class TransactionHandle private[host] (private val jHandle: JTransaction.TransactionHandle):

  /** Commit the transaction. */
  def commit(): Unit = jHandle.commit()

  /** Rollback the transaction. */
  def rollback(): Unit = jHandle.rollback()

  /** Close the transaction (commits if not already committed/rolled back). */
  def close(): Unit = jHandle.close()
