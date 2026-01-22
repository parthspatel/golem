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
 * Represents either a success value (Ok) or an error value (Err).
 *
 * This type provides ZIO-friendly operations and JSON serialization.
 */
sealed trait GolemResult[+E, +A]:
  def isOk: Boolean
  def isErr: Boolean = !isOk

  def toEither: Either[E, A]
  def toZIO: IO[E, A]

  def map[B](f: A => B): GolemResult[E, B]
  def mapError[F](f: E => F): GolemResult[F, A]
  def flatMap[E1 >: E, B](f: A => GolemResult[E1, B]): GolemResult[E1, B]

  def getOrElse[B >: A](default: => B): B
  def fold[B](onErr: E => B)(onOk: A => B): B

object GolemResult:
  final case class Ok[+A](value: A) extends GolemResult[Nothing, A]:
    def isOk: Boolean = true
    def toEither: Either[Nothing, A] = Right(value)
    def toZIO: IO[Nothing, A] = ZIO.succeed(value)

    def map[B](f: A => B): GolemResult[Nothing, B] = Ok(f(value))
    def mapError[F](f: Nothing => F): GolemResult[F, A] = this
    def flatMap[E1 >: Nothing, B](f: A => GolemResult[E1, B]): GolemResult[E1, B] = f(value)

    def getOrElse[B >: A](default: => B): B = value
    def fold[B](onErr: Nothing => B)(onOk: A => B): B = onOk(value)

  final case class Err[+E](error: E) extends GolemResult[E, Nothing]:
    def isOk: Boolean = false
    def toEither: Either[E, Nothing] = Left(error)
    def toZIO: IO[E, Nothing] = ZIO.fail(error)

    def map[B](f: Nothing => B): GolemResult[E, B] = this
    def mapError[F](f: E => F): GolemResult[F, Nothing] = Err(f(error))
    def flatMap[E1 >: E, B](f: Nothing => GolemResult[E1, B]): GolemResult[E1, B] = this

    def getOrElse[B >: Nothing](default: => B): B = default
    def fold[B](onErr: E => B)(onOk: Nothing => B): B = onErr(error)

  def ok[A](value: A): GolemResult[Nothing, A] = Ok(value)
  def err[E](error: E): GolemResult[E, Nothing] = Err(error)

  def fromEither[E, A](either: Either[E, A]): GolemResult[E, A] =
    either match
      case Right(a) => Ok(a)
      case Left(e)  => Err(e)

  def fromTry[A](t: scala.util.Try[A]): GolemResult[Throwable, A] =
    t match
      case scala.util.Success(a) => Ok(a)
      case scala.util.Failure(e) => Err(e)

  // ZIO conversions
  extension [E, A](result: GolemResult[E, A])
    def toZIOEither: UIO[Either[E, A]] = ZIO.succeed(result.toEither)

  // JSON codecs - using tagged union encoding
  given [E: JsonEncoder, A: JsonEncoder]: JsonEncoder[GolemResult[E, A]] =
    JsonEncoder[(String, Either[E, A])].contramap { result =>
      result match
        case Ok(value)  => ("ok", Right(value))
        case Err(error) => ("err", Left(error))
    }

  given [E: JsonDecoder, A: JsonDecoder]: JsonDecoder[GolemResult[E, A]] =
    JsonDecoder[(String, Either[E, A])].map {
      case ("ok", Right(value))  => Ok(value)
      case ("err", Left(error))  => Err(error)
      case (tag, _) => throw new RuntimeException(s"Invalid result tag: $tag")
    }
