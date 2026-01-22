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
 * Represents an optional value that may or may not be present.
 *
 * This type provides ZIO-friendly operations and JSON serialization.
 */
sealed trait GolemOption[+A]:
  def isSome: Boolean
  def isNone: Boolean = !isSome

  def toOption: Option[A]
  def toZIO: IO[NoSuchElementException, A]

  def map[B](f: A => B): GolemOption[B]
  def flatMap[B](f: A => GolemOption[B]): GolemOption[B]
  def filter(p: A => Boolean): GolemOption[A]

  def getOrElse[B >: A](default: => B): B
  def orElse[B >: A](alternative: => GolemOption[B]): GolemOption[B]

  def fold[B](ifNone: => B)(ifSome: A => B): B

object GolemOption:
  final case class Some[+A](value: A) extends GolemOption[A]:
    def isSome: Boolean = true
    def toOption: Option[A] = scala.Some(value)
    def toZIO: IO[NoSuchElementException, A] = ZIO.succeed(value)

    def map[B](f: A => B): GolemOption[B] = Some(f(value))
    def flatMap[B](f: A => GolemOption[B]): GolemOption[B] = f(value)
    def filter(p: A => Boolean): GolemOption[A] =
      if p(value) then this else None

    def getOrElse[B >: A](default: => B): B = value
    def orElse[B >: A](alternative: => GolemOption[B]): GolemOption[B] = this

    def fold[B](ifNone: => B)(ifSome: A => B): B = ifSome(value)

  case object None extends GolemOption[Nothing]:
    def isSome: Boolean = false
    def toOption: Option[Nothing] = scala.None
    def toZIO: IO[NoSuchElementException, Nothing] =
      ZIO.fail(new NoSuchElementException("GolemOption.None"))

    def map[B](f: Nothing => B): GolemOption[B] = None
    def flatMap[B](f: Nothing => GolemOption[B]): GolemOption[B] = None
    def filter(p: Nothing => Boolean): GolemOption[Nothing] = None

    def getOrElse[B >: Nothing](default: => B): B = default
    def orElse[B >: Nothing](alternative: => GolemOption[B]): GolemOption[B] = alternative

    def fold[B](ifNone: => B)(ifSome: Nothing => B): B = ifNone

  def some[A](value: A): GolemOption[A] = Some(value)
  def none[A]: GolemOption[A] = None

  def fromOption[A](opt: Option[A]): GolemOption[A] =
    opt match
      case scala.Some(v) => Some(v)
      case scala.None    => None

  def fromNullable[A](value: A | Null): GolemOption[A] =
    if value == null then None else Some(value.asInstanceOf[A])

  // ZIO conversions
  extension [A](opt: GolemOption[A])
    def toZIOOption: UIO[Option[A]] = ZIO.succeed(opt.toOption)

  // JSON codecs
  given [A: JsonEncoder]: JsonEncoder[GolemOption[A]] =
    JsonEncoder.option[A].contramap(_.toOption)

  given [A: JsonDecoder]: JsonDecoder[GolemOption[A]] =
    JsonDecoder.option[A].map(fromOption)
