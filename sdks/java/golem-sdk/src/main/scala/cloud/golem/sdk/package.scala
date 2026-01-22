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

package cloud.golem

import cloud.golem.sdk.types.{Option => JOption, Result => JResult, Either => JEither}

/**
 * Golem SDK for Scala.
 *
 * This package provides Scala-idiomatic wrappers around the Java SDK types.
 */
package object sdk {

  /**
   * Implicit conversions for Golem SDK types.
   */
  object implicits {

    /**
     * Converts Java Option to Scala Option.
     */
    implicit class OptionOps[T](private val opt: JOption[T]) extends AnyVal {
      def toScala: scala.Option[T] = {
        if (opt.isSome) scala.Some(opt.unwrap())
        else scala.None
      }
    }

    /**
     * Converts Scala Option to Java Option.
     */
    implicit class ScalaOptionOps[T](private val opt: scala.Option[T]) extends AnyVal {
      def toGolem: JOption[T] = opt match {
        case scala.Some(value) => JOption.some(value)
        case scala.None => JOption.none()
      }
    }

    /**
     * Converts Java Result to Scala Either.
     */
    implicit class ResultOps[T, E](private val result: JResult[T, E]) extends AnyVal {
      def toScala: scala.Either[E, T] = {
        if (result.isOk) scala.Right(result.unwrap())
        else scala.Left(result.unwrapErr())
      }
    }

    /**
     * Converts Scala Either to Java Result.
     */
    implicit class ScalaEitherOps[E, T](private val either: scala.Either[E, T]) extends AnyVal {
      def toResult: JResult[T, E] = either match {
        case scala.Right(value) => JResult.ok(value)
        case scala.Left(error) => JResult.err(error)
      }
    }

    /**
     * Converts Java Either to Scala Either.
     */
    implicit class EitherOps[L, R](private val either: JEither[L, R]) extends AnyVal {
      def toScala: scala.Either[L, R] = {
        if (either.isRight) scala.Right(either.unwrapRight())
        else scala.Left(either.unwrapLeft())
      }
    }

    /**
     * Converts Scala Either to Java Either.
     */
    implicit class ScalaEitherToGolemOps[L, R](private val either: scala.Either[L, R]) extends AnyVal {
      def toGolem: JEither[L, R] = either match {
        case scala.Right(value) => JEither.right(value)
        case scala.Left(value) => JEither.left(value)
      }
    }
  }

  /**
   * Transaction DSL for Scala.
   */
  object transaction {
    import cloud.golem.sdk.host.{Transaction => JTransaction}

    /**
     * Execute a block atomically.
     *
     * @param block The block to execute
     * @tparam T The return type
     * @return The result of the block
     */
    def atomically[T](block: => T): T = {
      JTransaction.atomically(() => block)
    }

    /**
     * Create a new transaction.
     *
     * @return A new Transaction instance
     */
    def begin(): JTransaction = JTransaction.begin()
  }
}
