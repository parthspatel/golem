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

package cloud.golem.zio.types

import zio.*
import zio.json.*

/**
 * Represents a value that can be one of two types: Left or Right.
 *
 * By convention, Left is used for "failure" or "alternative" cases,
 * while Right is the "success" or "primary" case.
 *
 * This type provides ZIO-friendly operations and JSON serialization.
 */
sealed trait GolemEither[+L, +R]:
  def isLeft: Boolean
  def isRight: Boolean = !isLeft

  def toEither: Either[L, R]
  def toZIO: IO[L, R]

  def map[R2](f: R => R2): GolemEither[L, R2]
  def mapLeft[L2](f: L => L2): GolemEither[L2, R]
  def flatMap[L1 >: L, R2](f: R => GolemEither[L1, R2]): GolemEither[L1, R2]

  def fold[C](onLeft: L => C)(onRight: R => C): C
  def swap: GolemEither[R, L]

object GolemEither:
  final case class Left[+L](value: L) extends GolemEither[L, Nothing]:
    def isLeft: Boolean = true
    def toEither: Either[L, Nothing] = scala.Left(value)
    def toZIO: IO[L, Nothing] = ZIO.fail(value)

    def map[R2](f: Nothing => R2): GolemEither[L, R2] = this
    def mapLeft[L2](f: L => L2): GolemEither[L2, Nothing] = Left(f(value))
    def flatMap[L1 >: L, R2](f: Nothing => GolemEither[L1, R2]): GolemEither[L1, R2] = this

    def fold[C](onLeft: L => C)(onRight: Nothing => C): C = onLeft(value)
    def swap: GolemEither[Nothing, L] = Right(value)

  final case class Right[+R](value: R) extends GolemEither[Nothing, R]:
    def isLeft: Boolean = false
    def toEither: Either[Nothing, R] = scala.Right(value)
    def toZIO: IO[Nothing, R] = ZIO.succeed(value)

    def map[R2](f: R => R2): GolemEither[Nothing, R2] = Right(f(value))
    def mapLeft[L2](f: Nothing => L2): GolemEither[L2, R] = this
    def flatMap[L1 >: Nothing, R2](f: R => GolemEither[L1, R2]): GolemEither[L1, R2] = f(value)

    def fold[C](onLeft: Nothing => C)(onRight: R => C): C = onRight(value)
    def swap: GolemEither[R, Nothing] = Left(value)

  def left[L](value: L): GolemEither[L, Nothing] = Left(value)
  def right[R](value: R): GolemEither[Nothing, R] = Right(value)

  def fromEither[L, R](either: Either[L, R]): GolemEither[L, R] =
    either match
      case scala.Right(r) => Right(r)
      case scala.Left(l)  => Left(l)

  // ZIO conversions
  extension [L, R](either: GolemEither[L, R])
    def toZIOEither: UIO[Either[L, R]] = ZIO.succeed(either.toEither)

  // JSON codecs
  given [L: JsonEncoder, R: JsonEncoder]: JsonEncoder[GolemEither[L, R]] =
    JsonEncoder[(String, Either[L, R])].contramap { either =>
      either match
        case Left(value)  => ("left", scala.Left(value))
        case Right(value) => ("right", scala.Right(value))
    }

  given [L: JsonDecoder, R: JsonDecoder]: JsonDecoder[GolemEither[L, R]] =
    JsonDecoder[(String, Either[L, R])].map {
      case ("left", scala.Left(value))   => Left(value)
      case ("right", scala.Right(value)) => Right(value)
      case (tag, _) => throw new RuntimeException(s"Invalid either tag: $tag")
    }
